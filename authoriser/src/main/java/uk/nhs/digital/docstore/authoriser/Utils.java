package uk.nhs.digital.docstore.authoriser;

import java.nio.charset.StandardCharsets;

public class Utils {

    public static String decodeURL(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
