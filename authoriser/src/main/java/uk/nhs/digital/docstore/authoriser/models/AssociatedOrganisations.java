package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociatedOrganisations {
    private List<Organisation> organisations;

    public AssociatedOrganisations(
            @JsonProperty("custom:nhsid_user_orgs") List<Organisation> organisations) {
        this.organisations = organisations;
    }

    public List<Organisation> getOrganisations() {
        return organisations;
    }
}
