package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organisation {

    @JsonProperty("ods_code")
    private String odsCode;

    @JsonProperty("org_name")
    private String orgName;

    @JsonProperty("org_type")
    private String orgType;

    public Organisation(String orgName, String orgType) {
        this.orgName = orgName;
        this.orgType = orgType;
    }
    public static boolean containsOrganisation(List<Organisation> organisations, String code) {
        return organisations.stream().anyMatch(org -> org.getOdsCode().equals(code));
    }
}
