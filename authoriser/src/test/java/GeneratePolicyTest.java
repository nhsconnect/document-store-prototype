import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.AuthConfig;
import uk.nhs.digital.docstore.authoriser.PolicyGenerator;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Role;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratePolicyTest {

    @Test
    void shouldReturnAllowedPolicyForPCSE() {
        var pcseResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalResources = List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(pcseResources, clinicalResources);

        var organisations = List.of(new Organisation("X4S4L", "some-org-name"));
        var roles = List.of(new Role("some-role-code"));

        var generatePolicy = new PolicyGenerator(authConfig, organisations, roles);

        var expectedPolicyDocument = IamPolicyResponse.PolicyDocument.builder()
                .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                .withStatement(pcseResources.stream().map(IamPolicyResponse::allowStatement).collect(Collectors.toList()))
                .withStatement(clinicalResources.stream().map(IamPolicyResponse::denyStatement).collect(Collectors.toList()))
                .build();

        var actual = generatePolicy.getPolicyDocument();

        assertThat(actual).usingRecursiveComparison().isEqualTo(expectedPolicyDocument);
    }
}
