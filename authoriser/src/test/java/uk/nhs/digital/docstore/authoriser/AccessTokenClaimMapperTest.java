package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.authoriser.models.AssociatedOrganisations;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.RbacRoles;
import uk.nhs.digital.docstore.authoriser.models.Role;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessTokenClaimMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGetAssociatedOrganisationsFromTokenClaims() throws JsonProcessingException, InvalidAccessTokenException {
        var associatedOrganisations = new AssociatedOrganisations(List.of(
                new Organisation("test", "test")
        ));

        var claim = objectMapper.writeValueAsString(associatedOrganisations);
        var token = JWT.create()
                .withClaim(Authoriser.ASSOCIATED_ORG, claim)
                .sign(Algorithm.none());
        var decodedToken = JWT.decode(token);

        var mapper = new AccessTokenClaimMapper(decodedToken);

        assertThatJson(mapper.deserialiseClaim(Authoriser.ASSOCIATED_ORG, AssociatedOrganisations.class)).isEqualTo(claim);
    }

    @Test
    void shouldThrowInvalidAccessTokenExceptionWhenAssociatedOrganisationsClaimIsMissing() {
        var token = JWT.create()
                .withSubject("test")
                .sign(Algorithm.none());
        var decodedToken = JWT.decode(token);

        var mapper = new AccessTokenClaimMapper(decodedToken);

        assertThrows(InvalidAccessTokenException.class, () -> {
            mapper.deserialiseClaim(Authoriser.ASSOCIATED_ORG, AssociatedOrganisations.class);
        });
    }

    @Test
    void shouldThrowInvalidAccessTokenExceptionWhenAssociatedOrganisationsClaimIsInvalid() {
        var token = JWT.create()
                .withClaim(Authoriser.ASSOCIATED_ORG, "invalid[json")
                .sign(Algorithm.none());
        var decodedToken = JWT.decode(token);

        var mapper = new AccessTokenClaimMapper(decodedToken);

        assertThrows(InvalidAccessTokenException.class, () -> {
            mapper.deserialiseClaim(Authoriser.ASSOCIATED_ORG, AssociatedOrganisations.class);
        });
    }

    AbstractStringAssert<?> assertThatJson(Object value) throws JsonProcessingException {
        return assertThat(objectMapper.writeValueAsString(value));
    }
}