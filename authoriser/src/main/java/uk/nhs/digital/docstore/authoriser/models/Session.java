package uk.nhs.digital.docstore.authoriser.models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.AccessTokenHash;
import com.nimbusds.openid.connect.sdk.claims.SessionID;
import java.time.Instant;
import java.util.UUID;

@DynamoDBTable(tableName = "ARFAuth")
public class Session {
    private String pk;
    private UUID id;
    private String sk;
    private Instant timeToExist;
    private String role;
    private String oidcSubject;

    private String oidcSessionID;
    private String accessTokenHash;
    public static final String PARTITION_KEY_PREFIX = "OIDCSUBJECT#";
    public static final String SORT_KEY_PREFIX = "SESSION#";

    public static Session create(
            UUID id, Instant timeToExist, Subject subject, SessionID sessionID, AccessTokenHash accessTokenHash) {
        var session = new Session();
        session.setId(id);
        session.setPK(PARTITION_KEY_PREFIX + subject.getValue());
        session.setSK(SORT_KEY_PREFIX + id);
        session.setTimeToExist(timeToExist);
        session.setOIDCSubject(subject.getValue());
        session.setOidcSessionID(sessionID.getValue());
        session.setAccessTokenHash(accessTokenHash.getValue());

        return session;
    }

    public static Session create(
            UUID id, Instant timeToExist, Subject subject, SessionID sessionID) {
        var session = new Session();
        session.setId(id);
        session.setPK(PARTITION_KEY_PREFIX + subject.getValue());
        session.setSK(SORT_KEY_PREFIX + id);
        session.setTimeToExist(timeToExist);
        session.setOIDCSubject(subject.getValue());
        session.setOidcSessionID(sessionID.getValue());

        return session;
    }

    public void setAccessTokenHash(String accessTokenHash) {
        this.accessTokenHash = accessTokenHash;
    }

    public String getAccessTokenHash() { return accessTokenHash; }

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

    @DynamoDBTypeConverted(converter = timeToExistConverter.class)
    @DynamoDBAttribute(attributeName = "TimeToExist")
    public Instant getTimeToExist() {
        return timeToExist;
    }

    public void setTimeToExist(Instant timeToExist) {
        this.timeToExist = timeToExist;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOIDCSubject() {
        return oidcSubject;
    }

    public void setOIDCSubject(String oidcSubject) {
        this.oidcSubject = oidcSubject;
    }

    public String getOidcSessionID() {
        return oidcSessionID;
    }

    public void setOidcSessionID(String oidcSessionID) {
        this.oidcSessionID = oidcSessionID;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != Session.class) return false;
        return id == ((Session) obj).getId();
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

    public static class timeToExistConverter implements DynamoDBTypeConverter<Long, Instant> {
        @Override
        public Long convert(Instant object) {
            return object.getEpochSecond();
        }

        @Override
        public Instant unconvert(Long seconds) {
            return Instant.ofEpochSecond(seconds);
        }
    }
}
