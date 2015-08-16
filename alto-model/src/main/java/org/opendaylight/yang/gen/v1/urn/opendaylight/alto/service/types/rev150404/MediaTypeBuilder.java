package org.opendaylight.yang.gen.v1.urn.opendaylight.alto.service.types.rev150404;


/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 *
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 *
 */
public class MediaTypeBuilder {

    public static MediaType getDefaultInstance(java.lang.String defaultValue) {
        if ("application/alto-directory+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoDirectory);
        } else if ("application/alto-networkmap+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoNetworkmap);
        } else if ("application/alto-networkmapfilter+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoNetworkmapfilter);
        } else if ("application/alto-costmap+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoCostmap);
        } else if ("application/alto-costmapfilter+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoCostmapfilter);
        } else if ("application/alto-endpointprop+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoEndpointprop);
        } else if ("application/alto-endpointpropparams+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoEndpointpropparams);
        } else if ("application/alto-endpointcost+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoEndpointcost);
        } else if ("application/alto-endpointcostparams+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoEndpointcostparams);
        } else if ("application/alto-error+json".equals(defaultValue)) {
            return new MediaType(MediaType.Enumeration.AltoError);
        }

        throw new java.lang.UnsupportedOperationException("Wrong MediaType: "+defaultValue);
    }

}
