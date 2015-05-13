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
}
