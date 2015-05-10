#Alto-Manager

======

Alto-manager provides extends karaf shell interface for you to create and delete maps and set properties for odl datastore. All functions are implemented by restconf in the backend.

Alto-Manager supports three different commands.

##alto:create
Load maps from file and put them into odl datastore

#####Command:

```
alto:create <map-type> <file-path>
```
alto:create command supports three different map types, `network-map, cost-map and endpoint-property-map`.

#####File Format:
* **network-map**: JSON Array of RFC formatted network maps.
* **cost-map**: JSON Array of RFC formatted cost maps.
* **endpoint-property-map**: Single RFC formatted endpoint property map

File examples can be found at ./alto-manager/examples/.

#####Note:
Exceptions will be thrown if you try to:

* Sepecify wrong map-type

#####Example:
```
alto:create network-map ./examples/network-map-rfc
alto:create cost-map ./examples/cost-map-rfc
alto:create endpoint-property-map ./examples/cost-map-rfc
```


##alto:delete
Delete map from odl datastore.

#####Command:

```
alto:delete <map-type> <resource-id or null>
```
alto:delete command supports three different map types, `network-map, cost-map and endpoint-property-map`.

If you are going to delete a network map or cost map, the second option should be set to resource id of the map. If you are going to delete endpoint-property-map, the second option should not be set.

#####Note:
Exceptions will be thrown if you try to:

* Sepecify unsupported map type
* Delete a map which does not exist
* Delete the default network map
* Delete a cost map or network map without specifying the resource id
* Delete the endpoint propery map with a resource id

#####Example:
```
alto:delete network-map my-default-network-map
alto:create cost-map new-network-map-routingcost-numerical
alto:create endpoint-property-map
```

##alto:set
Set specific field for old datastore. **Currently only "default-network-map" field for IRD resource is supported.**

#####Command:

```
alto:set <propety-name> <property-value>
```

#####Note:
Exceptions will be thrown if you try to:

* Sepecify unsupported map type
* Network map specified by resource id does not exist

#####Example:
```
alto:set default-network-map <network-map-resource-id>
```

##TODO
* Support URI for alto:create
