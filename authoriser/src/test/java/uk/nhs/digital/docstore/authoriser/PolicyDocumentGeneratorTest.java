package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Role;

public class PolicyDocumentGeneratorTest {

    @Test
    void shouldReturnCorrectPolicyForPCSE() {
        var pcseResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalResources =
                List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(pcseResources, clinicalResources);

        var organisations = List.of(new Organisation("some-org-name", "X4S4L", "some-org-type"));
        var roles = List.of(new Role("some-role-code"));

        var generatePolicy = new PolicyDocumentGenerator(authConfig, organisations, roles);

        List<IamPolicyResponse.Statement> statements = new ArrayList<>();
        statements.addAll(
                pcseResources.stream()
                        .map(IamPolicyResponse::allowStatement)
                        .collect(Collectors.toList()));
        statements.addAll(
                clinicalResources.stream()
                        .map(IamPolicyResponse::denyStatement)
                        .collect(Collectors.toList()));

        var expectedPolicyDocument =
                IamPolicyResponse.PolicyDocument.builder()
                        .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                        .withStatement(statements)
                        .build();

        var actual = generatePolicy.getPolicyDocument();

        assertThat(actual).usingRecursiveComparison().isEqualTo(expectedPolicyDocument);
    }

    @Test
    void shouldReturnCorrectPolicyForClinicalUser() {
        var pcseResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalResources =
                List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(pcseResources, clinicalResources);

        var organisations =
                List.of(new Organisation("some-org-code", "some-org-name", "some-org-type"));
        var roles = List.of(new Role("role:role:R8008"));

        var generatePolicy = new PolicyDocumentGenerator(authConfig, organisations, roles);

        List<IamPolicyResponse.Statement> statements = new ArrayList<>();
        statements.addAll(
                clinicalResources.stream()
                        .map(IamPolicyResponse::allowStatement)
                        .collect(Collectors.toList()));
        statements.addAll(
                pcseResources.stream()
                        .map(IamPolicyResponse::denyStatement)
                        .collect(Collectors.toList()));

        var expectedPolicyDocument =
                IamPolicyResponse.PolicyDocument.builder()
                        .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                        .withStatement(statements)
                        .build();

        var actual = generatePolicy.getPolicyDocument();

        assertThat(actual).usingRecursiveComparison().isEqualTo(expectedPolicyDocument);
    }

    @Test
    void shouldDenyEverythingIfUserIsntPCSEOrClinicalAdmin() {
        var pcseResources = List.of("api-gateway-invocation-arn-1", "api-gateway-invocation-arn-2");
        var clinicalResources =
                List.of("api-gateway-invocation-arn-3", "api-gateway-invocation-arn-4");
        var authConfig = new AuthConfig(pcseResources, clinicalResources);

        var organisations =
                List.of(new Organisation("some-org-code", "some-org-name", "some-org-type"));
        var roles = List.of(new Role("some-role-code"));

        var generatePolicy = new PolicyDocumentGenerator(authConfig, organisations, roles);

        var expectedPolicyDocument =
                IamPolicyResponse.PolicyDocument.builder()
                        .withVersion(IamPolicyResponse.VERSION_2012_10_17)
                        .withStatement(List.of(IamPolicyResponse.denyStatement("*")))
                        .build();

        var actual = generatePolicy.getPolicyDocument();

        assertThat(actual).usingRecursiveComparison().isEqualTo(expectedPolicyDocument);
    }
}
