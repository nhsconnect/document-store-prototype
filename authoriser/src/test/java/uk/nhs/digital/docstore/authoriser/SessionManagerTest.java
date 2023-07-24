package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import java.time.Instant;
import java.util.*;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.builders.IDTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.ProspectiveOrg;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;

class SessionManagerTest {

    @Test
    public void throwsErrorIfTokenCannotBeExchanged() throws LoginException {
        var oidcClient = Mockito.mock(OIDCClient.class);
        var sessionStore = new InMemorySessionStore();
        Mockito.when(oidcClient.authoriseSession(Mockito.any()))
                .thenThrow(new AuthorisationException(new Exception()));

        var sessionManager = new SessionManager(oidcClient, sessionStore);

        Assertions.assertThatThrownBy(() -> sessionManager.createSession(new AuthorizationCode()))
                .isInstanceOf(AuthorisationException.class);
    }

    @Test
    public void throwsErrorIfUserInfoRequestFails() throws LoginException {
        var sessionStore = new InMemorySessionStore();
        var oidcClient = Mockito.mock(OIDCClient.class);
        var session = Mockito.mock((Session.class));
        Mockito.when(session.getAccessTokenHash()).thenReturn("Access token");
        Mockito.when(session.getSubClaim()).thenReturn("Sub claim");
        Mockito.when(oidcClient.authoriseSession(Mockito.any())).thenReturn(session);
        Mockito.when(oidcClient.fetchUserInfo(Mockito.anyString(), Mockito.anyString()))
                .thenThrow(new UserInfoFetchingException("User info exception"));

        var sessionManager = new SessionManager(oidcClient, sessionStore);

        Assertions.assertThatThrownBy(() -> sessionManager.createSession(new AuthorizationCode()))
                .isInstanceOf(UserInfoFetchingException.class);
    }

    @Test
    public void savesSessionIfUserHasOneOrMoreValidOrgs() throws LoginException {
        var sessionStore = new InMemorySessionStore();
        var authCode = new AuthorizationCode("authcode");
        var jsonDataExtractor = Mockito.mock(JSONDataExtractor.class);
        var odsApiRequestClient = Mockito.mock(ODSAPIRequestClient.class);
        var oidcClient = Mockito.mock(OIDCClient.class);
        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();

        var session = Session.create(UUID.randomUUID(), claimsSet, new BearerAccessToken());

        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);
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
                        oidcClient, sessionStore, jsonDataExtractor, odsApiRequestClient);

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
    public void doesNotSaveSessionIfUserHasNoValidOrgs() throws LoginException {
        var sessionStore = new InMemorySessionStore();
        var authCode = new AuthorizationCode("authcode");
        var jsonDataExtractor = Mockito.mock(JSONDataExtractor.class);
        var odsApiRequestClient = Mockito.mock(ODSAPIRequestClient.class);
        var oidcClient = Mockito.mock(OIDCClient.class);
        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();

        var session = Session.create(UUID.randomUUID(), claimsSet, new BearerAccessToken());

        Mockito.when(oidcClient.authoriseSession(authCode)).thenReturn(session);
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
                        oidcClient, sessionStore, jsonDataExtractor, odsApiRequestClient);

        var result = sessionManager.createSession(authCode);

        var optionalSession =
                sessionStore.load(
                        new Subject(result.getSession().getOIDCSubject()),
                        result.getSession().getId());
        Assertions.assertThat(optionalSession).isEmpty();
    }
}
