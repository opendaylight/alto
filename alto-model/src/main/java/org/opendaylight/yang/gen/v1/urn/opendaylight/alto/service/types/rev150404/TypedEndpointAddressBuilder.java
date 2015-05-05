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
public class TypedEndpointAddressBuilder {

    public static TypedEndpointAddress getDefaultInstance(java.lang.String defaultValue) {
        if (defaultValue.startsWith("ipv4")) {
            return new TypedEndpointAddress(new TypedIpv4Address(defaultValue));
        } else if (defaultValue.startsWith("ipv6")) {
            return new TypedEndpointAddress(new TypedIpv6Address(defaultValue));
        }
        throw new java.lang.UnsupportedOperationException("Wrong TypedEndpointAddress type");
    }

}
