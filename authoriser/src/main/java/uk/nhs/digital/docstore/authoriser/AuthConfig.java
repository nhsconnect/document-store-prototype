package uk.nhs.digital.docstore.authoriser;

import java.util.List;

public class AuthConfig {
    private List<String> resourcesForPCSEUsers;
    private List<String> resourcesForClinicalUsers;

    public AuthConfig(List<String> resourcesForPCSEUsers, List<String> resourcesForClinicalUsers) {
        this.resourcesForPCSEUsers = resourcesForPCSEUsers;
        this.resourcesForClinicalUsers = resourcesForClinicalUsers;
    }

    public AuthConfig() {}

    public List<String> getResourcesForPCSEUsers() {
        return resourcesForPCSEUsers;
    }

    public List<String> getResourcesForClinicalUsers() {
        return resourcesForClinicalUsers;
    }
}
