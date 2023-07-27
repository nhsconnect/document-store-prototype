package uk.nhs.digital.docstore.authoriser.handlers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientMetadata;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public abstract class BaseAuthRequestHandler {
    private static final String AWS_REGION = "eu-west-2";
    private static final String DEFAULT_ENDPOINT = "";

    protected static AmazonDynamoDB getDynamodbClient() {
        var clientBuilder = AmazonDynamoDBClientBuilder.standard();
        var dynamodbEndpoint = System.getenv("DYNAMODB_ENDPOINT");
        if (!dynamodbEndpoint.equals(DEFAULT_ENDPOINT)) {
            clientBuilder =
                    clientBuilder.withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(
                                    dynamodbEndpoint, AWS_REGION));
        }
        return clientBuilder.build();
    }

    protected static DynamoDBMapper createDynamoDbMapper() {
        return new DynamoDBMapper(
                getDynamodbClient(),
                DynamoDBMapperConfig.builder().withTableNameOverride(tableNameOverrider()).build());
    }

    protected static OIDCClientInformation getClientInformation() {
        var env = System.getenv();
        var clientMetadata = new OIDCClientMetadata();
        try {
            clientMetadata.setRedirectionURI(new URI(env.get("OIDC_CALLBACK_URL")));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return new OIDCClientInformation(
                new ClientID(env.get("OIDC_CLIENT_ID")),
                null,
                clientMetadata,
                new Secret(env.get("OIDC_CLIENT_SECRET")));
    }

    protected static OIDCProviderMetadata getProviderMetadata() {
        var env = System.getenv();
        OIDCProviderMetadata providerMetadata;
        try {
            providerMetadata =
                    new OIDCProviderMetadata(
                            new Issuer(env.get("OIDC_ISSUER_URL")),
                            List.of(SubjectType.PUBLIC),
                            new URI(env.get("OIDC_JWKS_URL")));
            providerMetadata.setAuthorizationEndpointURI(new URI(env.get("OIDC_AUTHORIZE_URL")));
            providerMetadata.setTokenEndpointURI(new URI(env.get("OIDC_TOKEN_URL")));
            providerMetadata.setUserInfoEndpointURI(new URI(env.get("OIDC_USER_INFO_URL")));
            providerMetadata.setScopes(
                    new Scope("openid", "profile", "nationalrbacaccess", "associatedorgs"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return providerMetadata;
    }

    protected static String httpOnlyCookieBuilder(
            String fieldName, String fieldContents, Long maxAgeInSeconds) {
        return cookieBuilder(fieldName, fieldContents, maxAgeInSeconds) + "; HttpOnly";
    }

    protected static String cookieBuilder(
            String fieldName, String fieldContents, Long maxAgeInSeconds) {
        return fieldName
                + "="
                + fieldContents
                + "; SameSite=None; Secure; Path=/; Max-Age="
                + maxAgeInSeconds;
    }

    private static DynamoDBMapperConfig.TableNameOverride tableNameOverrider() {
        var workspace = System.getenv("WORKSPACE");
        String prefix = workspace != null && !workspace.isEmpty() ? workspace.concat("_") : "";
        return DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(prefix);
    }
}
