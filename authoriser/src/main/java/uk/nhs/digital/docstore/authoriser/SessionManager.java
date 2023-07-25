package uk.nhs.digital.docstore.authoriser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.authoriser.audit.message.UserInfoAuditMessage;
import uk.nhs.digital.docstore.authoriser.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.authoriser.audit.publisher.SplunkPublisher;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;

public class SessionManager {
    private final OIDCClient authenticationClient;

    private final SessionStore sessionStore;

    private final JSONDataExtractor jsonDataExtractor;

    private final ODSAPIRequestClient odsApiClient;

    private final AuditPublisher sensitiveIndex;

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    // for live code (we don't need to know about the JSON extractor or ODS client)
    public SessionManager(OIDCClient authenticationClient, SessionStore sessionStore) {
        this(
                authenticationClient,
                sessionStore,
                new JSONDataExtractor(),
                new ODSAPIRequestClient(),
                new SplunkPublisher(System.getenv("SQS_AUDIT_QUEUE_URL")));
    }

    // for testing
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

    public LoginEventResponse createSession(AuthorizationCode authCode)
            throws AuthorisationException, UserInfoFetchingException {
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

        HashMap<String, List<String>> gpAndPcseOrgs = new HashMap<>();

        odsCodes.forEach(
                code -> {
                    var orgData = odsApiClient.getResponse(code);
                    var orgRoles = jsonDataExtractor.getGpAndPcseRolesFromOrgData(orgData);
                    if (!orgRoles.isEmpty()) {
                        gpAndPcseOrgs.put(code, orgRoles);
                    }
                });

        if (!gpAndPcseOrgs.isEmpty()) {
            sessionStore.save(session);
        }

        return new LoginEventResponse(session, gpAndPcseOrgs);
    }
}
