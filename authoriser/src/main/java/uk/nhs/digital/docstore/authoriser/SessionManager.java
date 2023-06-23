package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import uk.nhs.digital.docstore.authoriser.enums.LoginEventOutcome;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.LoginEventResponse;
import uk.nhs.digital.docstore.authoriser.repository.SessionStore;

public class SessionManager {
    private final OIDCClient authenticationClient;
    private final SessionStore sessionStore;

    private final JSONDataExtractor jsonDataExtractor;

    private final ODSAPIRequestClient odsApiClient;

    // for live code (we don't need to know about the JSON extractor or ODS client)
    public SessionManager(OIDCClient authenticationClient, SessionStore sessionStore) {
        this(
                authenticationClient,
                sessionStore,
                new JSONDataExtractor(),
                new ODSAPIRequestClient());
    }

    // for testing
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

    public LoginEventResponse createSession(AuthorizationCode authCode)
            throws AuthorisationException, UserInfoFetchingException {
        var session = authenticationClient.authoriseSession(authCode);
        var userInfo =
                new JSONObject(
                        authenticationClient.fetchUserInfo(
                                session.getAccessTokenHash(), session.getSubClaim()));

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

        // if it's empty then the user can't log in
        // if one org then see if the user has a valid role
        // if more than one org then redirect the user to the org selection screen (see what data we
        // need to show to the user, presumably name of org and ODS code?)
        if (gpAndPcseOrgs.isEmpty()) {
            return new LoginEventResponse(session, LoginEventOutcome.NO_VALID_ORGS);
        } else if (gpAndPcseOrgs.size() == 1) {
            sessionStore.save(session);
            return new LoginEventResponse(session, LoginEventOutcome.ONE_VALID_ORG);
        } else {
            sessionStore.save(session);
            return new LoginEventResponse(session, LoginEventOutcome.MULTIPLE_VALID_ORGS);
        }
    }
}
