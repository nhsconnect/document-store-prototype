package uk.nhs.digital.docstore.authoriser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import java.time.Instant;
import java.util.*;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.audit.message.UserInfoAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.authoriser.builders.IDTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.ProspectiveOrg;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
class SessionManagerTest {
    private static final String SQS_URL = "some-url";
    private static final String SQS_ENDPOINT = "some-endpoint";

    @SystemStub
    private EnvironmentVariables environmentVariables =
            new EnvironmentVariables()
                    .set("SQS_AUDIT_QUEUE_URL", SQS_URL)
                    .set("SQS_ENDPOINT", SQS_ENDPOINT);

    private static InMemorySessionStore sessionStore;
    private final AuditPublisher splunkPublisher = Mockito.mock(AuditPublisher.class);
    private final OIDCClient oidcClient = Mockito.mock(OIDCClient.class);
    private final JSONDataExtractor jsonDataExtractor = Mockito.mock(JSONDataExtractor.class);
    private final ODSAPIRequestClient odsApiRequestClient = Mockito.mock(ODSAPIRequestClient.class);
    private final AuthorizationCode authCode = new AuthorizationCode("authcode");
    private final UserInfoAuditMessage auditMessage = new UserInfoAuditMessage("user-id");

    @BeforeEach
    public void init() {
        sessionStore = new InMemorySessionStore();
    }

    @Test
    public void throwsErrorIfTokenCannotBeExchanged() throws LoginException {
        Mockito.when(oidcClient.authoriseSession(Mockito.any()))
                .thenThrow(new AuthorisationException(new Exception()));

        var sessionManager = new SessionManager(oidcClient, sessionStore);

        Assertions.assertThatThrownBy(() -> sessionManager.createSession(new AuthorizationCode()))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void throwsErrorIfUserInfoRequestFails() throws LoginException, JsonProcessingException {
        var sessionStore = new InMemorySessionStore();
        var oidcClient = Mockito.mock(OIDCClient.class);
        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var session = Session.create(UUID.randomUUID(), claimsSet, new BearerAccessToken());

        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);
        Mockito.doNothing().when(splunkPublisher).publish(auditMessage);
        Mockito.when(oidcClient.fetchUserInfo(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new UserInfoFetchingException("User info exception"));

        var sessionManager =
                new SessionManager(
                        oidcClient,
                        sessionStore,
                        jsonDataExtractor,
                        odsApiRequestClient,
                        splunkPublisher);

        Assertions.assertThatThrownBy(() -> sessionManager.createSession(authCode))
                .isInstanceOf(UserInfoFetchingException.class);
    }

    @Test
    public void savesSessionIfUserHasOneOrMoreValidOrgs()
            throws LoginException, JsonProcessingException {
        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();
        var session = Session.create(UUID.randomUUID(), claimsSet, new BearerAccessToken());

        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);
        Mockito.doNothing().when(splunkPublisher).publish(auditMessage);
        Mockito.when(oidcClient.fetchUserInfo(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new UserInfo(new Subject()));
        Mockito.when(jsonDataExtractor.getOdsCodesFromUserInfo(Mockito.any(JSONObject.class)))
                .thenReturn(new ArrayList<>(Arrays.asList("Org1", "Org2")));
        Mockito.when(odsApiRequestClient.getResponse("Org1")).thenReturn(new JSONObject());
        Mockito.when(odsApiRequestClient.getResponse("Org2")).thenReturn(new JSONObject());
        Mockito.when(jsonDataExtractor.getProspectiveOrgs(Mockito.any(JSONObject.class)))
                .thenReturn(Optional.of(new ProspectiveOrg("ODS", "Name", PermittedOrgs.GPP)));

        var sessionManager =
                new SessionManager(
                        oidcClient,
                        sessionStore,
                        jsonDataExtractor,
                        odsApiRequestClient,
                        splunkPublisher);

        var result = sessionManager.createSession(authCode);

        var optionalSession =
                sessionStore.load(
                        new Subject(result.getSession().getOIDCSubject()),
                        result.getSession().getId());
        Assertions.assertThat(optionalSession).isPresent();
        var actualSession = optionalSession.get();

        Assertions.assertThat(actualSession.getTimeToExist())
                .isEqualTo(Instant.ofEpochMilli(claimsSet.getExpirationTime().getTime()));
        Assertions.assertThat(session.getOIDCSubject())
                .isEqualTo(claimsSet.getSubject().getValue());
        Assertions.assertThat(session.getOidcSessionID())
                .isEqualTo(claimsSet.getSessionID().getValue());
    }

    @Test
    public void doesNotSaveSessionIfUserHasNoValidOrgs()
            throws LoginException, JsonProcessingException {
        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();

        var session = Session.create(UUID.randomUUID(), claimsSet, new BearerAccessToken());

        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);
        Mockito.doNothing().when(splunkPublisher).publish(auditMessage);
        Mockito.when(oidcClient.fetchUserInfo(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new UserInfo(new Subject()));
        Mockito.when(jsonDataExtractor.getOdsCodesFromUserInfo(Mockito.any(JSONObject.class)))
                .thenReturn(new ArrayList<>(Arrays.asList("Org1", "Org2")));
        Mockito.when(odsApiRequestClient.getResponse("Org1")).thenReturn(new JSONObject());
        Mockito.when(odsApiRequestClient.getResponse("Org2")).thenReturn(new JSONObject());
        Mockito.when(jsonDataExtractor.getProspectiveOrgs(Mockito.any(JSONObject.class)))
                .thenReturn(Optional.empty());

        var sessionManager =
                new SessionManager(
                        oidcClient,
                        sessionStore,
                        jsonDataExtractor,
                        odsApiRequestClient,
                        splunkPublisher);

        var result = sessionManager.createSession(authCode);

        var optionalSession =
                sessionStore.load(
                        new Subject(result.getSession().getOIDCSubject()),
                        result.getSession().getId());
        Assertions.assertThat(optionalSession).isEmpty();
    }
}
