package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {
    private String role;

    public Role(@JsonProperty("role_code") String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
