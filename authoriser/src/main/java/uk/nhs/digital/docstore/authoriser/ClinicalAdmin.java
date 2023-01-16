package uk.nhs.digital.docstore.authoriser;

public enum ClinicalAdmin {
    R8008("R8008"), R8010("R8010"), R8007("R8007"), R8000("R8000"), R0008("R0008"), R8003("R8003"), R8013("R8013"), R8009("R8009");

    private final String clinicalRoleCode;

    ClinicalAdmin(String clinicalRoleCode) {
        this.clinicalRoleCode = clinicalRoleCode;
    }

    public String getClinicalRoleCode() {
        return clinicalRoleCode;
    }
}
