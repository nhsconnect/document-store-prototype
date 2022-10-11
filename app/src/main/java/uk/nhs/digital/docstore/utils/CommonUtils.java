package uk.nhs.digital.docstore.utils;

import java.util.UUID;

public class CommonUtils {

    public static String generateRandomUUIDString() {
        return UUID.randomUUID().toString();
    }
}
