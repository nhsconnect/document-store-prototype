package uk.nhs.digital.docstore.authoriser.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class Organisation {

    @JsonProperty("ods_code")
    private String odsCode;

    @JsonProperty("org_name")
    private String orgName;

    @JsonProperty("org_type")
    private String orgType;

    public static boolean containsOrganisation(List<Organisation> organisations, String code) {
        return organisations.stream().anyMatch(org -> org.getOdsCode().equals(code));
    }
}
