package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {
    private String role;

    public Role(@JsonProperty("role_code") String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static boolean containsAnyTertiaryRole(Role role, List<String> selectedRoleCodes) {
        for (String selectedRole : selectedRoleCodes) {
            var parts = role.getRole().split(":");
            var tertiaryCode = parts[parts.length - 1];

            if (selectedRole.equals(tertiaryCode)) {
                return true;
            }
        }
        return false;
    }
}
