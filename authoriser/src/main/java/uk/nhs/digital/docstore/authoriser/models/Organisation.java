package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Organisation {
    private final String orgCode;

    private final String orgName;

    public Organisation(@JsonProperty("org_code") String orgCode, @JsonProperty("org_name") String orgName) {
        this.orgCode = orgCode;
        this.orgName = orgName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public String getOrgName() {
        return orgName;
    }
}