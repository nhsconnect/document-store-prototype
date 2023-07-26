package uk.nhs.digital.docstore.authoriser.enums;

import java.util.Arrays;

public enum PermittedOrgs {
    PCSE("Primary Care Support England", "RO157"),
    GPP("GP Practice", "RO76"),
    DEV("Dev", "RO198"),
    UNKNOWN("Unknown", "");

    public final String type;
    public final String roleCode;

    PermittedOrgs(String type, String roleCode) {
        this.type = type;
        this.roleCode = roleCode;
    }

    public static PermittedOrgs fromType(String orgType) {
        return Arrays.stream(PermittedOrgs.values())
                .filter(scanResult -> scanResult.type.equals(orgType))
                .findAny()
                .orElse(UNKNOWN);
    }

    public static PermittedOrgs fromRoleCode(String roleCode) {
        return Arrays.stream(PermittedOrgs.values())
                .filter(scanResult -> scanResult.type.equals(roleCode))
                .findAny()
                .orElse(UNKNOWN);
    }
}
