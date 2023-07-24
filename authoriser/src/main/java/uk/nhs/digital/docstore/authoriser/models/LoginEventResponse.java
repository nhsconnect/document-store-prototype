package uk.nhs.digital.docstore.authoriser.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
@AllArgsConstructor
public class LoginEventResponse {
    private final Session session;
    private final List<ProspectiveOrg> usersOrgs;
}
