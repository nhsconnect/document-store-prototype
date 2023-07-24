package uk.nhs.digital.docstore.authoriser.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;
@Getter
@AllArgsConstructor
public class ProspectiveOrg {

    private final String odsCode;
    private final String orgName;
    private final PermittedOrgs orgType;

}
