package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import uk.nhs.digital.docstore.authoriser.exceptions.LoginException;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.models.Organisation;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;

public class SessionManager {
    private final OIDCClient authenticationClient;
    private final SessionStore sessionStore;

    private final JSONDataExtractor jsonDataExtractor;

    private final ODSAPIRequestClient odsApiClient;

    public SessionManager(OIDCClient authenticationClient, SessionStore sessionStore) {
        this(
                authenticationClient,
                sessionStore,
                new JSONDataExtractor(),
                new ODSAPIRequestClient());
    }

    public SessionManager(
            OIDCClient authenticationClient,
            SessionStore sessionStore,
            JSONDataExtractor jsonDataExtractor,
            ODSAPIRequestClient odsApiClient) {
        this.authenticationClient = authenticationClient;
        this.sessionStore = sessionStore;
        this.jsonDataExtractor = jsonDataExtractor;
        this.odsApiClient = odsApiClient;
    }

    public LoginEventResponse createSession(AuthorizationCode authCode) throws LoginException {
        var session = authenticationClient.authoriseSession(authCode);
        var userInfo =
                new JSONObject(
                        authenticationClient
                                .fetchUserInfo(session.getAccessTokenHash(), session.getSubClaim())
                                .toJSONString());
        System.out.println("user info object: " + userInfo);
        var odsCodes = jsonDataExtractor.getOdsCodesFromUserInfo(userInfo);

        List<Organisation> prospectiveOrgs = odsCodes.stream()
                .map(odsApiClient::getResponse)
                .map(jsonDataExtractor::getProspectiveOrgs)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (!prospectiveOrgs.isEmpty()) {
            sessionStore.save(session);
        }

        return new LoginEventResponse(session, prospectiveOrgs);
    }
}
