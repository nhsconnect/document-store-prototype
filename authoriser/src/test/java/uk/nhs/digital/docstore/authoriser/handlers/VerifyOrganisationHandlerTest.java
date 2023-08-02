package uk.nhs.digital.docstore.authoriser.handlers;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.Context;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.authoriser.builders.IDTokenClaimsSetBuilder;
import uk.nhs.digital.docstore.authoriser.enums.PermittedOrgs;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.models.Session;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;
import uk.nhs.digital.docstore.authoriser.requestEvents.OrganisationRequestEvent;
import uk.nhs.digital.docstore.authoriser.stubs.InMemorySessionStore;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class VerifyOrganisationHandlerTest {
    @SystemStub
    private EnvironmentVariables environmentVariables =
            new EnvironmentVariables().set("WORKSPACE", "test");

    private final SessionStore inMemorySessionStore = new InMemorySessionStore();
    private VerifyOrganisationHandler handler;
    private Session session;
    private final String odsCode = "A100";
    private final long maxCookieAgeInSeconds = 100L;

    @BeforeEach
    public void init() {
        var clock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        var fixedTime = Instant.now(clock);
        var cookieExpiryTime = fixedTime.plusSeconds(maxCookieAgeInSeconds);
        var claimsSet = IDTokenClaimsSetBuilder.buildClaimsSet();

        session = Session.create(UUID.randomUUID(), claimsSet, new BearerAccessToken());
        session.setRole("Role");
        session.setOIDCSubject("subject");
        session.setTimeToExist(cookieExpiryTime);
        session.setAccessTokenHash("AccesstokenHash");
        session.setOrganisations(
                List.of(new Organisation(odsCode, "Name", PermittedOrgs.GPP.type)));

        inMemorySessionStore.save(session);

        handler = new VerifyOrganisationHandler(inMemorySessionStore, clock);
    }

    @Test
    void handleRequestReturnsCookiesAndUserTypeForValidOrgUser() {
        var request = new OrganisationRequestEvent();
        request.setQueryStringParameters(Map.of("organisation[organisation]", odsCode));
        request.setHeaders(
                Map.of(
                        "Cookie",
                        String.format(
                                "SubjectClaim=%s; SessionId=%s",
                                session.getSubClaim(), session.getId())));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        var expectedResponse = String.format("{\"UserType\":\"%s\"}", PermittedOrgs.GPP.type);

        Assertions.assertEquals(200, response.getStatusCode());
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getMultiValueHeaders().get("Set-Cookie"))
                .contains(
                        "UserType="
                                + PermittedOrgs.GPP.type
                                + "; SameSite=None; Secure; Path=/; Max-Age="
                                + maxCookieAgeInSeconds
                                + "; HttpOnly");
        Assertions.assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void handleRequestInvalidSubjectClaimOrSessionIdReturnsNotFound() {
        var request = new OrganisationRequestEvent();
        request.setQueryStringParameters(Map.of("organisation[organisation]", odsCode));
        request.setHeaders(
                Map.of(
                        "Cookie",
                        String.format(
                                "SubjectClaim=%s; SessionId=%s",
                                UUID.randomUUID(), UUID.randomUUID())));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertEquals(404, response.getStatusCode());
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void handleRequestInvalidOdsCodeReturnsBadRequest() {
        var request = new OrganisationRequestEvent();
        request.setQueryStringParameters(Map.of("organisation[organisation]", "invalid"));
        request.setHeaders(
                Map.of(
                        "Cookie",
                        String.format(
                                "SubjectClaim=%s; SessionId=%s",
                                session.getSubClaim(), session.getId())));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertEquals(400, response.getStatusCode());
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void handleRequestInvalidOrgQueryParamsReturnsBadRequest() {
        var request = new OrganisationRequestEvent();
        request.setQueryStringParameters(Map.of("invalid", "invalid"));
        request.setHeaders(
                Map.of(
                        "Cookie",
                        String.format(
                                "SubjectClaim=%s; SessionId=%s",
                                session.getSubClaim(), session.getId())));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertEquals(400, response.getStatusCode());
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void handleRequestMissingSubjectClaimReturnsBadRequest() {
        var request = new OrganisationRequestEvent();
        request.setQueryStringParameters(Map.of("organisation[organisation]", odsCode));
        request.setHeaders(Map.of("Cookie", String.format("SessionId=%s", session.getId())));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertEquals(400, response.getStatusCode());
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void handleRequestMissingSessionIdReturnsBadRequest() {
        var request = new OrganisationRequestEvent();
        request.setQueryStringParameters(Map.of("organisation[organisation]", odsCode));
        request.setHeaders(
                Map.of("Cookie", String.format("SubjectClaim=%s", session.getSubClaim())));

        var response = handler.handleRequest(request, Mockito.mock(Context.class));

        Assertions.assertEquals(400, response.getStatusCode());
        assertThat(response.getIsBase64Encoded()).isFalse();
        assertThat(response.getBody()).isEmpty();
    }
}
