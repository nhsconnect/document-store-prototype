package uk.nhs.digital.docstore.authoriser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.audit.message.UserInfoAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.authoriser.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;

import java.util.ArrayList;

public class SessionManager {
    private final OIDCClient authenticationClient;

    private final SessionStore sessionStore;

    private final JSONDataExtractor jsonDataExtractor;

    private final ODSAPIRequestClient odsApiClient;

    private final AuditPublisher sensitiveIndex;

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    public SessionManager(OIDCClient authenticationClient, SessionStore sessionStore) {
        this(
                authenticationClient,
                sessionStore,
                new JSONDataExtractor(),
                new ODSAPIRequestClient(),
                new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")));
    }

    public SessionManager(
            OIDCClient authenticationClient,
            SessionStore sessionStore,
            JSONDataExtractor jsonDataExtractor,
            ODSAPIRequestClient odsApiClient,
            AuditPublisher sensitiveIndex) {
        this.authenticationClient = authenticationClient;
        this.sessionStore = sessionStore;
        this.jsonDataExtractor = jsonDataExtractor;
        this.odsApiClient = odsApiClient;
        this.sensitiveIndex = sensitiveIndex;
    }

    public LoginEventResponse createSession(AuthorizationCode authCode) throws LoginException {
        var session = authenticationClient.authoriseSession(authCode);

        try {
            sensitiveIndex.publish(new UserInfoAuditMessage(session.getId().toString()));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error publishing to Splunk, malformed JSON: {}", e.getMessage());
        }

        var userInfo =
                new JSONObject(
                        authenticationClient
                                .fetchUserInfo(session.getAccessTokenHash(), session.getSubClaim())
                                .toJSONString());
        System.out.println("user info object: " + userInfo);
        var odsCodes = jsonDataExtractor.getOdsCodesFromUserInfo(userInfo);

        List<Organisation> prospectiveOrgs =
                odsCodes.stream()
                        .map(odsApiClient::getResponse)
                        .map(jsonDataExtractor::getProspectiveOrgs)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        prospectiveOrgs.clear();

        if (!prospectiveOrgs.isEmpty()) {
            session.setOrganisations(prospectiveOrgs);
            sessionStore.save(session);
        }

        return new LoginEventResponse(session, prospectiveOrgs);
    }
}
