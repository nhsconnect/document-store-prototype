package uk.nhs.digital.docstore.authoriser.enums;

public enum PermittedOrgs {
    PCSE("Primary Care Support England", "RO157"),
    GPP("GP Practice", "RO76"),
    DEV("Dev", "RO198");
    public final String type;
    public final String roleCode;

    PermittedOrgs(String type, String roleCode) {
        this.type = type;
        this.roleCode = roleCode;
    }
}
