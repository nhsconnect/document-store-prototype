package uk.nhs.digital.docstore.authoriser;

import java.util.List;

public class AuthConfig {
    private final List<String> allowedResourcesForPCSEUsers;
    private final List<String> allowedResourcesForClinicalUsers;

    //TODO avoid need to know about these constructor parameter names in terraform
    public AuthConfig (List<String> allowedResourcesForPCSEUsers, List<String> allowedResourcesForClinicalUsers) {
        this.allowedResourcesForPCSEUsers = allowedResourcesForPCSEUsers;
        this.allowedResourcesForClinicalUsers = allowedResourcesForClinicalUsers;
    }

    public List<String> getAllowedResourcesForPCSEUsers() {
        return allowedResourcesForPCSEUsers;
    }

    public List<String> getAllowedResourcesForClinicalUsers() {
        return allowedResourcesForClinicalUsers;
    }
}
