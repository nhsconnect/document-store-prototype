package uk.nhs.digital.docstore.authoriser;

import java.util.List;

public class AuthConfig {
    private  List<String> allowedResourcesForPCSEUsers;
    private  List<String> allowedResourcesForClinicalUsers;

    //TODO avoid need to know about these constructor parameter names in terraform
    public AuthConfig (List<String> allowedResourcesForPCSEUsers, List<String> allowedResourcesForClinicalUsers) {
        this.allowedResourcesForPCSEUsers = allowedResourcesForPCSEUsers;
        this.allowedResourcesForClinicalUsers = allowedResourcesForClinicalUsers;
    }

    public AuthConfig(){
    }

    public List<String> getResourcesForPCSEUsers() {
        return allowedResourcesForPCSEUsers;
    }

    public List<String> getResourcesForClinicalUsers() {
        return allowedResourcesForClinicalUsers;
    }
}
