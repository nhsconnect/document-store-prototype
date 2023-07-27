package uk.nhs.digital.docstore.authoriser.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserID {
    private final String userID;

    public UserID(@JsonProperty("uid") String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }
}
