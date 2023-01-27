package uk.nhs.digital.docstore.authoriser;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessTokenClaimMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWTValidator.class);
    private final DecodedJWT jwt;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccessTokenClaimMapper(DecodedJWT jwt) {
        this.jwt = jwt;
    }

    public <T> T deserialiseClaim(String claimName, Class<T> className) throws InvalidAccessTokenException {
        LOGGER.debug("claim name: " + claimName );
        String claimValue = jwt.getClaim(claimName).asString();
        LOGGER.debug("claim value: " + claimValue );
        try{
            return mapper.readValue(claimValue, className);
        }catch(JsonProcessingException | IllegalArgumentException e){
            LOGGER.debug("an exception happened when deserialise claim" + e );
            throw new InvalidAccessTokenException();
        }
    }
}
