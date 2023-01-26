package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RbacRoles {
    private List<Role> roles;

    public RbacRoles(@JsonProperty("custom:nhsid_nrbac_roles") List<Role> roles) {
        this.roles = roles;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public boolean containsAnyTertiaryRole(List<String> selectedRoleCodes) {
        for (Role role : roles) {
            for (String selectedRole : selectedRoleCodes) {
                var parts = role.getRole().split(":");
                var tertiaryCode = parts[parts.length - 1];

                if (selectedRole.equals(tertiaryCode)) {
                    return true;
                }
            }
        }
        return false;
    }
}
