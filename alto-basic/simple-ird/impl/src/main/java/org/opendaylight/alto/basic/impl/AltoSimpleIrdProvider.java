/*
 * Copyright © 2017 SNLab and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.alto.basic.impl;

import org.opendaylight.alto.basic.simpleird.SimpleIrdUtils;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRoute;
import org.opendaylight.alto.core.northbound.api.AltoNorthboundRouter;
import org.opendaylight.alto.core.resourcepool.ResourcepoolUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.Information;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.InformationBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstance;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfiguration;
import org.opendaylight.yang.gen.v1.urn.alto.simple.ird.rev151021.IrdInstanceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.alto.types.rev150921.ResourceId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public class AltoSimpleIrdProvider {

    public static final String ROOT_INSTANCE = "root";
    public static final String SIMPLE_IRD_ROUTE_NAME = "simpleird";

    private static final Logger LOG = LoggerFactory.getLogger(AltoSimpleIrdProvider.class);

    private final DataBroker mDataBroker;

    InstanceIdentifier<IrdInstanceConfiguration> mIID = null;
    InstanceIdentifier<IrdInstance> mRootIID = null;

    private String mContext = null;
    private SimpleIrdListener mListener = null;
    private AltoNorthboundRouter mRouter = null;
    private AltoNorthboundRoute mRoute = null;

    protected void createContext() throws InterruptedException, ExecutionException {
        WriteTransaction wx = mDataBroker.newWriteOnlyTransaction();

        mContext = ResourcepoolUtils.getUUID(SIMPLE_IRD_ROUTE_NAME);
        ResourcepoolUtils.createContext(mContext, wx);

        InformationBuilder builder = new InformationBuilder();
        builder.setContextId(new Uuid(mContext));

        InstanceIdentifier<Information> infoIID;
        infoIID = InstanceIdentifier.builder(Information.class).build();

        wx.put(LogicalDatastoreType.OPERATIONAL, infoIID, builder.build());
        wx.submit().get();

        LOG.info("Registered context {} for SimpleIrd", mContext);
    }

    protected void setupListener() {
        mListener = new SimpleIrdListener(new Uuid(mContext));
        mListener.register(mDataBroker, mIID);
    }

    protected void createDefaultIrd() throws InterruptedException, ExecutionException {
        WriteTransaction wx = mDataBroker.newWriteOnlyTransaction();

        IrdInstanceConfigurationBuilder builder = new IrdInstanceConfigurationBuilder();
        builder.setEntryContext(ResourcepoolUtils.getDefaultContextIID())
                .setInstanceId(new ResourceId(SimpleIrdUtils.DEFAULT_IRD_RESOURCE));

        InstanceIdentifier<IrdInstanceConfiguration> iicIID =
                SimpleIrdUtils.getInstanceConfigurationIID(new ResourceId(SimpleIrdUtils.DEFAULT_IRD_RESOURCE));

        wx.put(LogicalDatastoreType.CONFIGURATION, iicIID, builder.build());
        wx.submit().get();

        LOG.info("Create default IRD context for SimpleIrd");
    }

    protected void deleteContext() throws InterruptedException, ExecutionException {
        WriteTransaction wx = mDataBroker.newWriteOnlyTransaction();
        ResourcepoolUtils.deleteContext(mContext, wx);

        wx.submit().get();
    }

    protected void closeListener() throws Exception {
        if (mListener != null) {
            mListener.close();
        }
    }

    public AltoSimpleIrdProvider(final DataBroker dataBroker, final AltoNorthboundRouter router) {
        this.mDataBroker = dataBroker;
        this.mRouter = router;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        mIID = SimpleIrdUtils.getInstanceConfigurationListIID();
        mRootIID = SimpleIrdUtils.getInstanceIID(ROOT_INSTANCE);

        try {
            createContext();
            setupListener();
            createDefaultIrd();
            setupRoute(mRouter);
        } catch (Exception e) {
            LOG.error("Failed to create top-level containers");
            e.printStackTrace();
        }
        LOG.info("AltoSimpleIrdProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        try {
            if (mRouter != null) {
                mRouter.removeRoute(SIMPLE_IRD_ROUTE_NAME);
            }

            closeListener();
            deleteContext();
        } catch (Exception e) {
        }
        LOG.info("AltoSimpleIrdProvider Closed");
    }

    IrdInstance getInstance(ResourceId rid) {
        ReadTransaction rx = mDataBroker.newReadOnlyTransaction();

        try {
            return SimpleIrdUtils.readInstance(rid, rx);
        } catch (Exception e) {
            return null;
        }
    }

    public void setupRoute(AltoNorthboundRouter router) {
        AltoNorthboundRoute route = new SimpleIrdRoute(this);
        String base_url = router.addRoute(SIMPLE_IRD_ROUTE_NAME, route);
        if (base_url == null) {
            LOG.error("Failed to register route for AltoSimpleIrd");
            return;
        }

        try {
            WriteTransaction wx = mDataBroker.newWriteOnlyTransaction();

            InstanceIdentifier<Information> infoIID;
            infoIID = InstanceIdentifier.builder(Information.class).build();
            InformationBuilder builder = new InformationBuilder();

            builder.setBaseUrl(base_url);
            wx.merge(LogicalDatastoreType.OPERATIONAL, infoIID, builder.build());

            wx.submit().get();

            mRoute = route;
        } catch (Exception e) {
            LOG.error("Failed to reigster route");
        }
    }
}