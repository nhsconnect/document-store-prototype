package uk.nhs.digital.docstore.authoriser.enums;

public enum PermittedOrgTypes {
    PCSE("Primary Care Support England", "RO157"),
    GPP("GP Practice", "RO76"),
    DEV("Dev", "RO198");
    public final String orgDescription;
    public final String roleCode;

    PermittedOrgTypes(String orgDescription, String roleCode) {
        this.orgDescription = orgDescription;
        this.roleCode = roleCode;
    }
}
