package uk.nhs.digital.docstore.authoriser;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import uk.nhs.digital.docstore.authoriser.exceptions.AuthorisationException;
import uk.nhs.digital.docstore.authoriser.exceptions.UserInfoFetchingException;
import uk.nhs.digital.docstore.authoriser.models.Session;

public class SessionManager {
    OIDCClient authenticationClient;

    public SessionManager(OIDCClient authenticationClient) {
        this.authenticationClient = authenticationClient;
    }

    public Session createSession(AuthorizationCode authCode)
            throws AuthorisationException, UserInfoFetchingException {
        var session = authenticationClient.authoriseSession(authCode);
        var userInfo =
                new JSONObject(
                        authenticationClient.fetchUserInfo(
                                session.getAccessTokenHash(), session.getSubClaim()));

        var odsCodes = JSONDataExtractor.getOdsCodesFromUserInfo(userInfo);
        System.out.println(odsCodes);

        HashMap<String, List<String>> orgDataMap = new HashMap<>();

        odsCodes.forEach(
                code -> {
                    try {
                        var orgData = ODSAPIClient.getOrgData(code);
                        var orgRoles = JSONDataExtractor.getRolesFromOrgData(orgData);
                        orgDataMap.put(code, orgRoles);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        return session;
    }
}
