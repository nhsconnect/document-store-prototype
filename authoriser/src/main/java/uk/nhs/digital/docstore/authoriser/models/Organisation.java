package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Organisation {
    private final String odsCode;
    private final String orgName;
    private final String orgType;

    public Organisation(
            @JsonProperty("org_name") String orgName,
            @JsonProperty("ods_code") String odsCode,
            @JsonProperty("org_type") String orgType) {
        this.odsCode = odsCode;
        this.orgName = orgName;
        this.orgType = orgType;
    }

    public String getOdsCode() {
        return odsCode;
    }

    public static boolean containsOrganisation(List<Organisation> organisations, String code) {
        return organisations.stream().anyMatch(org -> org.getOdsCode().equals(code));
    }
}
