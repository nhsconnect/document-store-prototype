package uk.nhs.digital.docstore.authoriser.models;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.math.BigInteger;

@DynamoDBTable(tableName = "ARFAuth")
public class Session {
    private String pk;
    private String id;
    private String sk;
    private BigInteger timeToExist;

    private String authStateParameter;

    public static Session create(String id, BigInteger timeToExist, String authStateParameter) {
        var session = new Session();
        session.setId(id);
        session.setPK("SESSION#" + id);
        session.setSK("SESSION#" + id);
        session.setTimeToExist(timeToExist);
        session.setAuthStateParameter(authStateParameter);

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

    @DynamoDBAttribute(attributeName = "Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "TimeToExist")
    public BigInteger getTimeToExist() {
        return timeToExist;
    }

    public void setTimeToExist(BigInteger timeToExist) {
        this.timeToExist = timeToExist;
    }

    @DynamoDBAttribute(attributeName = "AuthStateParameter")
    public String getAuthStateParameter() {
        return authStateParameter;
    }

    public void setAuthStateParameter(String authStateParameter) {
        this.authStateParameter = authStateParameter;
    }
}
