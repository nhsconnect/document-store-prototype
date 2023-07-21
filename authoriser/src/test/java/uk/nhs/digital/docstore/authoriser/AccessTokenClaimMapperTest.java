package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.exceptions.InvalidAccessTokenException;
import uk.nhs.digital.docstore.authoriser.models.AssociatedOrganisations;
import uk.nhs.digital.docstore.authoriser.models.Organisation;

class AccessTokenClaimMapperTest {

    public static final String ASSOCIATED_ORG = "custom:nhsid_user_orgs";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetAssociatedOrganisationsFromTokenClaims()
            throws JsonProcessingException, InvalidAccessTokenException {
        var associatedOrganisations =
                new AssociatedOrganisations(List.of(new Organisation("test", "test", "test")));

        var claim = objectMapper.writeValueAsString(associatedOrganisations);
        var token = JWT.create().withClaim(ASSOCIATED_ORG, claim).sign(Algorithm.none());
        var decodedToken = JWT.decode(token);

        var mapper = new AccessTokenClaimMapper(decodedToken);

        assertThatJson(mapper.deserialiseClaim(ASSOCIATED_ORG, AssociatedOrganisations.class))
                .isEqualTo(claim);
    }

    @Test
    void shouldThrowInvalidAccessTokenExceptionWhenAssociatedOrganisationsClaimIsMissing() {
        var token = JWT.create().withSubject("test").sign(Algorithm.none());
        var decodedToken = JWT.decode(token);

        var mapper = new AccessTokenClaimMapper(decodedToken);

        assertThrows(
                InvalidAccessTokenException.class,
                () -> {
                    mapper.deserialiseClaim(ASSOCIATED_ORG, AssociatedOrganisations.class);
                });
    }

    @Test
    void shouldThrowInvalidAccessTokenExceptionWhenAssociatedOrganisationsClaimIsInvalid() {
        var token = JWT.create().withClaim(ASSOCIATED_ORG, "invalid[json").sign(Algorithm.none());
        var decodedToken = JWT.decode(token);

        var mapper = new AccessTokenClaimMapper(decodedToken);

        assertThrows(
                InvalidAccessTokenException.class,
                () -> {
                    mapper.deserialiseClaim(ASSOCIATED_ORG, AssociatedOrganisations.class);
                });
    }

    AbstractStringAssert<?> assertThatJson(Object value) throws JsonProcessingException {
        return assertThat(objectMapper.writeValueAsString(value));
    }
}
