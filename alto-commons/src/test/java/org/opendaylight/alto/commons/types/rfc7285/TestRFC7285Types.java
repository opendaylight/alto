/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.rfc7285;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRFC7285Types {

    public RFC7285NetworkMap makeNetworkMap() {
        /*
         *
         *      {
         *          "meta" : {
         *              "vtag": {
         *                  "resource-id": "my-default-network-map",
         *                  "tag": "da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785"
         *              }
         *          },
         *          "network-map": {
         *              "PID1" : {
         *                  "ipv4" : [
         *                      "192.0.2.0/24",
         *                      "198.51.100.0/25"
         *                  ]
         *              },
         *              "PID2" : {
         *                  "ipv4" : [
         *                      "198.51.100.128/25"
         *                  ]
         *              },
         *              "PID3" : {
         *                  "ipv4" : [
         *                      "0.0.0.0/0"
         *                  ],
         *                  "ipv6" : [
         *                      "::/0"
         *                  ]
         *              }
         *          }
         *      }
         *
         * */
        RFC7285NetworkMap nm = new RFC7285NetworkMap();
        nm.meta.vtag = new RFC7285VersionTag("my-default-network-map",
                                             "da65eca2eb7a10ce8b059740b0b2e3f8eb1d4785");

        nm.map.put("PID1", new RFC7285Endpoint.AddressGroup());
        nm.map.get("PID1").ipv4.add("192.0.2.0/24");
        nm.map.get("PID1").ipv4.add("198.51.100.0/25");

        nm.map.put("PID2", new RFC7285Endpoint.AddressGroup());
        nm.map.get("PID2").ipv4.add("198.51.100.128/25");

        nm.map.put("PID3", new RFC7285Endpoint.AddressGroup());
        nm.map.get("PID3").ipv4.add("0.0.0.0/0");
        nm.map.get("PID3").ipv6.add("::/0");

        return nm;
    }

    @Test
    public void test() {
    }

    public <T> void assertCollectionEquals(Collection<T> lhs, Collection<T> rhs) {
        Set<T> _lhs = new HashSet<T>(lhs);
        Set<T> _rhs = new HashSet<T>(rhs);
        assertEquals(lhs.size(), rhs.size());

        for (T obj: lhs) {
            assertTrue(_rhs.contains(obj));
        }
    }

    @Test
    public void testNetworkMap() throws Exception {
        RFC7285JSONMapper mapper = new RFC7285JSONMapper();

        RFC7285NetworkMap nm = makeNetworkMap();
        String nmText = mapper.asJSON(nm);
        RFC7285NetworkMap _nm = mapper.asNetworkMap(nmText);

        assertEquals(nm.meta.vtag, _nm.meta.vtag);
        assertEquals(nm.map.size(), _nm.map.size());
        assertCollectionEquals(nm.map.get("PID1").ipv4, _nm.map.get("PID1").ipv4);
        assertCollectionEquals(nm.map.get("PID2").ipv4, _nm.map.get("PID2").ipv4);
        assertCollectionEquals(nm.map.get("PID3").ipv4, _nm.map.get("PID3").ipv4);
        assertCollectionEquals(nm.map.get("PID3").ipv6, _nm.map.get("PID3").ipv6);

        String addrGroupString = mapper.asJSON(nm.map.get("PID3"));
        RFC7285Endpoint.AddressGroup _ag = mapper.asAddressGroup(addrGroupString);
        assertCollectionEquals(nm.map.get("PID3").ipv4, _ag.ipv4);
        assertCollectionEquals(nm.map.get("PID3").ipv6, _ag.ipv6);
    }

    @Test
    public void testNetworkMapFilter() throws Exception {
        /*
         *
         *      {
         *          "pids": [ "PID1", "PID2" ]
         *      }
         * */

        RFC7285JSONMapper mapper = new RFC7285JSONMapper();

        RFC7285NetworkMap.Filter filter = new RFC7285NetworkMap.Filter();
        filter.pids = new ArrayList<String>();
        filter.pids.add("PID1");
        filter.pids.add("PID2");

        String nmfString = mapper.asJSON(filter);
        RFC7285NetworkMap.Filter _filter = mapper.asNetworkMapFilter(nmfString);
        assertCollectionEquals(filter.pids, _filter.pids);
    }

    public RFC7285CostMap makeCostMap() {
        /*
         *  {
         *      "meta": {
         *          "dependent-vtags" : [
         *              {
         *                  "resource-id": "my-default-network-map",
         *                  "tag": "3ee2cb7e8d63d9fab71b9b34cbf764436315542e"
         *              }
         *          ],
         *          "cost-type" : {
         *              "cost-mode": "numerical",
         *              "cost-metric": "routingcost"
         *          }
         *      },
         *      "cost-map" : {
         *          "PID1": { "PID1": 1,  "PID2": 5,  "PID3": 10 },
         *          "PID2": { "PID1": 5,  "PID2": 1,  "PID3": 15 },
         *          "PID3": { "PID1": 20, "PID2": 15  }
         *      }
         *  }
         * */

        RFC7285CostMap cm = new RFC7285CostMap();
        cm.meta.costType = new RFC7285CostType("numerical", "routingcost");
        cm.meta.netmap_tags.add(new RFC7285VersionTag("my-default-network-map",
                                                      "3ee2cb7e8d63d9fab71b9b34cbf764436315542e"));
        cm.map.put("PID1", new LinkedHashMap<String, Object>());
        cm.map.get("PID1").put("PID1", new Integer(1));
        cm.map.get("PID1").put("PID2", new Integer(5));
        cm.map.get("PID1").put("PID3", new Integer(10));
        cm.map.put("PID2", new LinkedHashMap<String, Object>());
        cm.map.get("PID2").put("PID1", new Integer(5));
        cm.map.get("PID2").put("PID2", new Integer(1));
        cm.map.get("PID2").put("PID3", new Integer(15));
        cm.map.put("PID3", new LinkedHashMap<String, Object>());
        cm.map.get("PID3").put("PID1", new Integer(20));
        cm.map.get("PID3").put("PID2", new Integer(15));

        return cm;
    }

    @Test
    public void testCostMap() throws Exception {
        RFC7285JSONMapper mapper = new RFC7285JSONMapper();

        RFC7285CostMap cm = makeCostMap();

        String cmString = mapper.asJSON(cm);
        RFC7285CostMap _cm = mapper.asCostMap(cmString);

        assertCollectionEquals(cm.meta.netmap_tags, _cm.meta.netmap_tags);
        assertEquals(cm.meta.costType, _cm.meta.costType);

        String pids[] = { "PID1", "PID2", "PID3" };
        for (String pid: pids) {
            assertCollectionEquals(cm.map.get(pid).entrySet(), _cm.map.get(pid).entrySet());
        }
    }

    @Test
    public void testCostMapFilter() throws Exception {
        /*
         *  {
         *      "cost-type" : {
         *          "cost-mode": "numerical",
         *          "cost-metric": "routingcost"
         *      },
         *      "pids" : {
         *          "srcs" : [ "PID1" ],
         *          "dsts" : [ "PID1", "PID2", "PID3" ]
         *      }
         *  }
         * */

        RFC7285JSONMapper mapper = new RFC7285JSONMapper();

        RFC7285CostMap.Filter filter = new RFC7285CostMap.Filter();
        filter.costType = new RFC7285CostType("numerical", "routingcost", "test");
        filter.pids = new RFC7285QueryPairs();
        filter.pids.src.add("PID1");
        filter.pids.dst.add("PID1");
        filter.pids.dst.add("PID2");
        filter.pids.dst.add("PID3");

        String cmfString = mapper.asJSON(filter);
        RFC7285CostMap.Filter _filter = mapper.asCostMapFilter(cmfString);

        assertEquals(filter.costType, _filter.costType);
        assertCollectionEquals(filter.pids.src, _filter.pids.src);
        assertCollectionEquals(filter.pids.dst, _filter.pids.dst);
    }

    @Test
    public void testECSRequest() throws Exception {
        /*
         *  {
         *      "cost-type" : {
         *          "cost-mode": "ordinal",
         *          "cost-metric": "routingcost"
         *      },
         *      "endpoints": {
         *          "srcs": [ "ipv4:192.0.2.2" ],
         *          "dsts": [
         *              "ipv4:192.0.2.89",
         *              "ipv4:198.51.100.34",
         *              "ipv4:203.0.113.45"
         *          ]
         *      }
         *  }
         * */

        RFC7285JSONMapper mapper = new RFC7285JSONMapper();

        RFC7285Endpoint.CostRequest req = new RFC7285Endpoint.CostRequest();
        req.costType = new RFC7285CostType("ordinal", "routingcost", "test");
        req.endpoints = new RFC7285QueryPairs();
        req.endpoints.src.add("ipv4:192.0.2.2");
        req.endpoints.dst.add("ipv4:192.0.2.89");
        req.endpoints.dst.add("ipv4:198.51.100.34");
        req.endpoints.dst.add("ipv4:203.0.113.45");

        String ecsrString = mapper.asJSON(req);
        RFC7285Endpoint.CostRequest _req = mapper.asCostRequest(ecsrString);

        assertEquals(req.costType, _req.costType);
        assertCollectionEquals(req.endpoints.src, _req.endpoints.src);
        assertCollectionEquals(req.endpoints.dst, _req.endpoints.dst);
    }

    public RFC7285Endpoint.CostResponse makeECSResponse() {
        /*
         *  {
         *      "meta": {
         *          "cost-type" : {
         *              "cost-mode": "ordinal",
         *              "cost-metric": "routingcost"
         *          }
         *      },
         *      "endpoint-cost-map" : {
         *          "ipv4:192.0.2.2": {
         *              "ipv4:192.0.2.89": 1,
         *              "ipv4:198.51.100.34": 2,
         *              "ipv4:203.0.113.45": 3
         *          },
         *      }
         *  }
         * */

        String src[] = { "ipv4:192.0.2.2" };
        String dst[] = { "ipv4:192.0.2.89", "ipv4:198.51.100.34", "ipv4:203.0.113.45" };

        RFC7285Endpoint.CostResponse ecsr = new RFC7285Endpoint.CostResponse();
        ecsr.meta.costType = new RFC7285CostType("ordinal", "routingcost");
        ecsr.answer = new LinkedHashMap<String, Map<String, Object>>();
        ecsr.answer.put(src[0], new LinkedHashMap<String, Object>());
        ecsr.answer.get(src[0]).put(dst[0], new Integer(1));
        ecsr.answer.get(src[0]).put(dst[1], new Integer(2));
        ecsr.answer.get(src[0]).put(dst[2], new Integer(3));

        return ecsr;
    }


    @Test
    public void testECSAnswer() throws Exception {
        RFC7285JSONMapper mapper = new RFC7285JSONMapper();

        RFC7285Endpoint.CostResponse ecsr = makeECSResponse();

        String ecsrString = mapper.asJSON(ecsr);
        RFC7285Endpoint.CostResponse _ecsr = mapper.asCostResponse(ecsrString);

        assertEquals(ecsr.meta.costType, _ecsr.meta.costType);

        String endpoints[] = { "ipv4:192.0.2.2" };
        for (String endpoint: endpoints) {
            assertCollectionEquals(ecsr.answer.get(endpoint).entrySet(),
                                   _ecsr.answer.get(endpoint).entrySet());
        }
    }
}
