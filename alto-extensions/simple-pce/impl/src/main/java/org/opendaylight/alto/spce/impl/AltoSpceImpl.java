/*
 * Copyright (c) 2015 SNLAB and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.spce.impl;

import org.opendaylight.alto.spce.impl.algorithm.PathComputation;
import org.opendaylight.alto.spce.impl.util.FlowManager;
import org.opendaylight.alto.spce.impl.util.InventoryReader;
import org.opendaylight.alto.spce.impl.util.MeterManager;
import org.opendaylight.alto.spce.impl.util.RouteManager;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.AltoSpceMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.AltoSpceService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.ErrorCodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.FlowType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.GetRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.GetRouteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.GetRouteOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.RemoveRateLimitingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.RemoveRateLimitingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.RemoveRateLimitingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.RemoveRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.RemoveRouteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.RemoveRouteOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.SetupRouteInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.SetupRouteOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.SetupRouteOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.UpdateRateLimitingInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.UpdateRateLimitingOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.UpdateRateLimitingOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.endpoints.group.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.endpoints.group.EndpointsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.setup.route.input.ConstraintMetric;
import org.opendaylight.yang.gen.v1.urn.opendaylight.alto.spce.rev160718.setup.route.input.ConstraintMetricBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.network.tracker.rev151107.NetworkTrackerService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TpId;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

public class AltoSpceImpl implements AltoSpceService {

    private static final Logger LOG = LoggerFactory.getLogger(FlowManager.class);
    private FlowManager flowManager;
    private MeterManager meterManager;
    private InventoryReader inventoryReader;
    private PathComputation pathComputation;
    private RouteManager routeManager;

    public AltoSpceImpl(SalMeterService salMeterService,
                        NetworkTrackerService networkTrackerService,
                        DataBroker dataBroker) {
        this.meterManager = new MeterManager(salMeterService, dataBroker);
        this.flowManager = new FlowManager(dataBroker);
        this.inventoryReader = new InventoryReader(dataBroker);
        this.pathComputation = new PathComputation(networkTrackerService);
        this.routeManager = new RouteManager(pathComputation, inventoryReader, networkTrackerService, flowManager, meterManager, dataBroker);
    }

    private List<ConstraintMetric> compressConstraint(List<ConstraintMetric> constraintMetrics) {
        if (constraintMetrics == null)
            return null;
        List<ConstraintMetric> compressedConstraintMetrics = new LinkedList<>();
        BigInteger minHopcount = BigInteger.ZERO;
        BigInteger maxHopcount = BigInteger.valueOf(Long.MAX_VALUE);
        BigInteger minBandwidth = BigInteger.ZERO;
        BigInteger maxBandwidth = BigInteger.valueOf(Long.MAX_VALUE);
        for (ConstraintMetric constraintMetric : constraintMetrics) {
            if (constraintMetric.getMetric() == AltoSpceMetric.Hopcount) {
                minHopcount = minHopcount.max(constraintMetric.getMin());
                maxHopcount = maxHopcount.min(constraintMetric.getMax());
                if (minHopcount.compareTo(maxHopcount) == 1) {
                    return null;
                }
            } else if (constraintMetric.getMetric() == AltoSpceMetric.Bandwidth) {
                minBandwidth = minBandwidth.max(constraintMetric.getMin());
                maxBandwidth = maxBandwidth.min(constraintMetric.getMax());
                if (minBandwidth.compareTo(maxBandwidth) == 1) {
                    return null;
                }
            }
        }
        compressedConstraintMetrics.add(new ConstraintMetricBuilder()
                .setMetric(AltoSpceMetric.Hopcount)
                .setMin(minHopcount)
                .setMax(maxHopcount)
                .build());
        compressedConstraintMetrics.add(new ConstraintMetricBuilder()
                .setMetric(AltoSpceMetric.Bandwidth)
                .setMin(minBandwidth)
                .setMax(maxBandwidth)
                .build());
        return compressedConstraintMetrics;
    }

    private String showRoute(Endpoints endpoint, List<TpId> path) {
        String pathString = endpoint.getSrc().getValue();
        if (path != null) {
            for (TpId tpId : path) {
                pathString += "|" + tpId.getValue();
            }
        }
        pathString += "|" + endpoint.getDst().getValue();
        return pathString;
    }

    @Override
    public Future<RpcResult<UpdateRateLimitingOutput>> updateRateLimiting(UpdateRateLimitingInput input) {
        ErrorCodeType errorCodeType = this.routeManager.updateRateLimiting(input.getEndpoints(), input.getLimitedRate(), input.getBurstSize());
        UpdateRateLimitingOutputBuilder builder = new UpdateRateLimitingOutputBuilder().setErrorCode(errorCodeType);
        if (errorCodeType==ErrorCodeType.OK) {
            builder.setRoute(showRoute(input.getEndpoints(),this.routeManager.getRoute(input.getEndpoints())));
        }
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<RemoveRateLimitingOutput>> removeRateLimiting(RemoveRateLimitingInput input) {
        ErrorCodeType errorCodeType = this.routeManager.removeRateLimiting(input.getEndpoints());
        RemoveRateLimitingOutputBuilder builder = new RemoveRateLimitingOutputBuilder().setErrorCode(errorCodeType);
        if (errorCodeType==ErrorCodeType.OK) {
            builder.setRoute(showRoute(input.getEndpoints(),this.routeManager.getRoute(input.getEndpoints())));
        }
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<RemoveRouteOutput>> removeRoute(RemoveRouteInput input) {
        String route = input.getRoute();
        String src = route.substring(0, route.indexOf('|'));
        String dst = route.substring(route.lastIndexOf('|')+1);
        Endpoints endpoints = new EndpointsBuilder()
                .setSrc(new Ipv4Address(src))
                .setDst(new Ipv4Address(dst))
                .build();

        ErrorCodeType errorCodeType = this.routeManager.removeRoute(endpoints);

        RemoveRouteOutputBuilder outputBuilder = new RemoveRouteOutputBuilder();
        outputBuilder.setErrorCode(errorCodeType);
        return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
    }

    @Override
    public Future<RpcResult<SetupRouteOutput>> setupRoute(SetupRouteInput input) {
        if (null == input.getFlowLayer() || null == input.getEndpoints()) {
            SetupRouteOutputBuilder outputBuilder = new SetupRouteOutputBuilder();
            outputBuilder.setErrorCode(ErrorCodeType.INVALIDINPUT);
            return RpcResultBuilder.success(outputBuilder.build()).buildFuture();
        }

        Endpoints endpoints = input.getEndpoints();
        Integer burstSize =  input.getBurstSize();
        Integer limitedRate = input.getLimitedRate();
        FlowType flowLayer = input.getFlowLayer();
        List<AltoSpceMetric> altoSpceMetrics = input.getObjectiveMetrics();
        List<ConstraintMetric> constraintMetrics = compressConstraint(input.getConstraintMetric());

        List<TpId> route;
        ErrorCodeType errorCode;

        route = this.routeManager.computeRoute(endpoints, altoSpceMetrics, constraintMetrics);

        if (route == null) {
            errorCode = ErrorCodeType.COMPUTINGROUTEERROR;
            SetupRouteOutput output = new SetupRouteOutputBuilder()
                    .setErrorCode(errorCode).build();
            return RpcResultBuilder.success(output).buildFuture();
        }

        if (burstSize == null || limitedRate == null || burstSize.intValue() < 0 || limitedRate.intValue() < 0) {
            errorCode = this.routeManager.setupRoute(endpoints, route, flowLayer, -1, -1);
        } else {
            errorCode = this.routeManager.setupRoute(endpoints, route, flowLayer, limitedRate, burstSize);
        }

        if (errorCode != ErrorCodeType.OK) {
            SetupRouteOutput output = new SetupRouteOutputBuilder()
                    .setErrorCode(errorCode).build();
            return RpcResultBuilder.success(output).buildFuture();
        } else {
            SetupRouteOutput output = new SetupRouteOutputBuilder()
                    .setRoute(showRoute(endpoints, route))
                    .setErrorCode(errorCode)
                    .build();
            return RpcResultBuilder.success(output).buildFuture();
        }
    }

    @Override
    public Future<RpcResult<GetRouteOutput>> getRoute(GetRouteInput input) {
        GetRouteOutputBuilder builder = new GetRouteOutputBuilder();
        if (null == input || null == input.getEndpoints()) {
            builder.setErrorCode(ErrorCodeType.INVALIDINPUT);
        } else {
            builder.setErrorCode(ErrorCodeType.OK);
            List<TpId> route = this.routeManager.getRoute(input.getEndpoints());
            if (null == route) {
                builder.setRoute("ROUTE_HAVE_NOT_BEEN_SET_UP");
            } else {
                builder.setRoute(showRoute(input.getEndpoints(), route));
            }
        }
        return RpcResultBuilder.success(builder.build()).buildFuture();
    }
}
