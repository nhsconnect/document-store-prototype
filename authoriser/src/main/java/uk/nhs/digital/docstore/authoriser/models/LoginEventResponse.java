package uk.nhs.digital.docstore.authoriser.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginEventResponse {
    private final Session session;
    private final List<ProspectiveOrg> usersOrgs;
}
