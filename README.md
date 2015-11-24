
# TODO

1. Use [routed RPC service][routed-rpc] to implement ALTO instances.  Also see
   [examples][routed-rpc-example].

[routed-rpc]: https://ask.opendaylight.org/question/99/how-does-request-routing-works/
[routed-rpc-example]: https://git.opendaylight.org/gerrit/gitweb?p=controller.git;a=blob;f=opendaylight/md-sal/sal-binding-it/src/test/java/org/opendaylight/controller/test/sal/binding/it/RoutedServiceTest.jav=d49d6f0e25e271e43c8550feb5eef63d9630118b=HEAD4a

# Test service models for ALTO

~~~
{
    "input": {
        "service-reference":"/alto-resourcepool:context[alto-resourcepool:context-id='00000000-0000-0000-0000-000000000000']/alto-resourcepool:resource[alto-resourcepool:resource-id='test-model-base']/alto-resourcepool:context-tag[alto-resourcepool:tag='NEED TO CHECK THE RESOURCEPOOL']"
    }
}


{
    "input": {
        "service-reference":"/alto-resourcepool:context[alto-resourcepool:context-id='00000000-0000-0000-0000-000000000000']/alto-resourcepool:resource[alto-resourcepool:resource-id='test-model-networkmap']/alto-resourcepool:context-tag[alto-resourcepool:tag='NEED TO CHECK THE RESOURCEPOOL']",
        "type":"alto-model-networkmap:resource-type-networkmap",
        "networkmap-filter": {
            "pid": ["PID1", "PID2", "PID3"],
            "address-type": [
                "alto-model-networkmap:address-type-ipv4",
                "alto-model-networkmap:address-type-ipv6"
            ]
        }
    }
}


{
    "input": {
        "service-reference":"/alto-resourcepool:context[alto-resourcepool:context-id='00000000-0000-0000-0000-000000000000']/alto-resourcepool:resource[alto-resourcepool:resource-id='test-model-endpointcost']/alto-resourcepool:context-tag[alto-resourcepool:tag='NEED TO CHECK THE RESOURCEPOOL']",
        "type":"alto-model-endpointcost:resource-type-endpointcost",
        "endpointcost-params": {
            "cost-type": {
                "cost-mode": "ordinal",
                "cost-metric": "routingcost"
            },
            "endpoint-filter": {
                "source": [
                    {"ipv4": "192.168.0.1"},
                    {"ipv4": "192.168.0.2"}
                ],
                "destination": [
                    {"ipv4": "192.168.1.1"},
                    {"ipv4": "192.168.1.2"}
                ]
            }
        }
    }
}
~~~


