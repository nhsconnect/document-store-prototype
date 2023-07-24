package uk.nhs.digital.docstore.authoriser.models;

import lombok.Getter;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;

@Getter
public class ProspectiveOrg {

    private final String odsCode;
    private final String orgName;
    private final PermittedOrgs orgType;

    public ProspectiveOrg(String odsCode, String orgName, PermittedOrgs orgType) {
        this.odsCode = odsCode;
        this.orgName = orgName;
        this.orgType = orgType;
    }
}
