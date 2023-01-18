package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.nhs.digital.docstore.authoriser.models.AssociatedOrganisations;
import uk.nhs.digital.docstore.authoriser.models.RbacRoles;

public class AccessTokenClaimMapper {
    private final DecodedJWT jwt;

    private final ObjectMapper mapper = new ObjectMapper();

    public AccessTokenClaimMapper(DecodedJWT jwt) {
        this.jwt = jwt;
    }

    public <T> T deserialiseClaim(String claimName, Class<T> className) throws InvalidAccessTokenException {
        String claimValue = jwt.getClaim(claimName).asString();
        try{
            return mapper.readValue(claimValue, className);
        }catch(JsonProcessingException | IllegalArgumentException e){
            throw new InvalidAccessTokenException();
        }
    }
}
