package uk.nhs.digital.docstore.authoriser.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.nimbusds.oauth2.sdk.id.State;
import java.util.UUID;

@DynamoDBTable(tableName = "ARFAuth")
public class Session {
    private String pk;
    private UUID id;
    private String sk;
    private long timeToExist;
    private State authStateParameter;

    private String role;

    public static final String KEY_PREFIX = "SESSION#";

    public static Session create(UUID id, Long timeToExist) {
        var session = new Session();
        session.setId(id);
        session.setPK(KEY_PREFIX + id);
        session.setSK(KEY_PREFIX + id);
        session.setTimeToExist(timeToExist);

        return session;
    }

    @DynamoDBHashKey(attributeName = "PK")
    public String getPK() {
        return pk;
    }

    public void setPK(String pk) {
        this.pk = pk;
    }

    @DynamoDBRangeKey(attributeName = "SK")
    public String getSK() {
        return sk;
    }

    public void setSK(String sk) {
        this.sk = sk;
    }

    @DynamoDBTypeConverted(converter = UUIDConverter.class)
    @DynamoDBAttribute(attributeName = "Id")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "TimeToExist")
    public Long getTimeToExist() {
        return timeToExist;
    }

    public void setTimeToExist(Long timeToExist) {
        this.timeToExist = timeToExist;
    }

    @DynamoDBTypeConverted(converter = StateConverter.class)
    @DynamoDBAttribute(attributeName = "AuthStateParameter")
    public State getAuthStateParameter() {
        return authStateParameter;
    }

    public void setAuthStateParameter(State authStateParameter) {
        this.authStateParameter = authStateParameter;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static class UUIDConverter implements DynamoDBTypeConverter<String, UUID> {
        @Override
        public String convert(UUID object) {
            return object.toString();
        }

        @Override
        public UUID unconvert(String uuid) {
            return UUID.fromString(uuid);
        }
    }

    public static class StateConverter implements DynamoDBTypeConverter<String, State> {
        @Override
        public String convert(State object) {
            return object.toString();
        }

        @Override
        public State unconvert(String state) {
            return new State(state);
        }
    }
}
