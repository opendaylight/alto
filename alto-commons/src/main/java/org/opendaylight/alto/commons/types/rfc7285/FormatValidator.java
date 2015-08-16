/*
 * Copyright (c) 2015 Yale University and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.alto.commons.types.rfc7285;

import java.util.regex.Pattern;

public class FormatValidator {

    /**
     * RFC 7285  section 10.1
     * */
    private static final String VALID_CHARSET = "a-zA-Z_0-9\\-:@";
    private static final Pattern VALID_ID_PATTERN
                            = Pattern.compile("^["+VALID_CHARSET+"]{1,64}$");
    private static final String VALID_CHARSET_WITH_DOT = VALID_CHARSET + "\\.";
    private static final Pattern VALID_ID_PATTERN_WITH_DOT
                            = Pattern.compile("^["+VALID_CHARSET_WITH_DOT+"]{1,64}$");
    private static final String VALID_TAG_CHARSET = "!-~";
    private static final Pattern VALID_TAG_PATTERN
                            = Pattern.compile("^["+VALID_TAG_CHARSET+"]{1,64}$");
    private static final String VALID_ADDR_IPV4 = "^ipv4:(([0-9]|[1-9][0-9]|1[0-9][0-9]|"
                                                    + "2[0-4][0-9]|25[0-5])\\.){3}"
                                                    + "([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])"
                                                    + "(%[\\p{N}\\p{L}]+)?$";
    private static final Pattern VALID_ADDR_IPV4_PATTERN
                            = Pattern.compile(VALID_ADDR_IPV4);
    private static final String VALID_ADDR_IPV6_1 = "^ipv6:((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}"
                                                    + "((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|"
                                                    + "(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}"
                                                    + "(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))"
                                                    + "(%[\\p{N}\\p{L}]+)?$";
    private static final Pattern VALID_ADDR_IPV6_1_PATTERN
                            = Pattern.compile(VALID_ADDR_IPV6_1);
    private static final String VALID_ADDR_IPV6_2 = "^ipv6:((([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|"
                                                    + "((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)"
                                                    + "(%.+)?)$";
    private static final Pattern VALID_ADDR_IPV6_2_PATTERN
                            = Pattern.compile(VALID_ADDR_IPV6_2);
    private static final String VALID_OPERATORS = "(gt|lt|ge|le|eq)";
    private static final Pattern VALID_CONSTRAINTS_PATTERN
                            = Pattern.compile("^"+VALID_OPERATORS+" [0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");


    public static boolean validId(String id) {
        return VALID_ID_PATTERN.matcher(id).matches();
    }

    public static boolean validIdWithDots(String id) {
            return VALID_ID_PATTERN_WITH_DOT.matcher(id).matches();
    }

    /**
     * RFC 7285 section 10.1
     * */
    public static boolean validPid(String id) {
        return validId(id);
    }

    public static boolean validPidWithDots(String id) {
        return validIdWithDots(id);
    }

    /**
     * RFC 7285 section 10.2
     * */
    public static boolean validResourceId(String id) {
        return validId(id);
    }

    public static boolean validResourceIdWithDots(String id) {
        return validIdWithDots(id);
    }

    /**
     * RFC 7285 section 10.3
     * */
    public static boolean validTag(String tag) {
        return VALID_TAG_PATTERN.matcher(tag).matches();
    }
    /**
     * RFC 7285 section 10.8.1
     * */
    public static boolean validSpecificEndpointProperty(String prop) {
        /* TODO  maybe enhance the performance? */
        return validId(prop) && (prop.indexOf('@') == -1);
    }

    public static boolean validSpecificEndpointPropertyWithDots(String prop) {
        /* TODO  maybe enhance the performance? */
        return validIdWithDots(prop) && (prop.indexOf('@') == -1);
    }

    /**
     * RFC 7285 seciton 10.8.2
     * */
    public static boolean validGlobalEndpointProperty(String prop) {
        return (prop.length() <= 32) && validId(prop);
    }

    /**
     * RFC 7285 section 11.3.2
     * */
    public static boolean validFilterConstraint(String constrant) {
        return VALID_CONSTRAINTS_PATTERN.matcher(constrant).matches();
    }

    public static boolean validAddressIpv4(String address) {
        return VALID_ADDR_IPV4_PATTERN.matcher(address).matches();
    }

    public static boolean validAddressIpv6(String address) {
        return VALID_ADDR_IPV6_1_PATTERN.matcher(address).matches() &&
                VALID_ADDR_IPV6_2_PATTERN.matcher(address).matches();
    }

    public static boolean validEndpointAddress(String address) {
        return validAddressIpv4(address) || validAddressIpv6(address);
    }
}
