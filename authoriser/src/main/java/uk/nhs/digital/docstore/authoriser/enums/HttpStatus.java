package uk.nhs.digital.docstore.authoriser.enums;

public enum HttpStatus {
    OK(200),
    SEE_OTHER(303),
    BAD_REQUEST(400),
    UNAUTHORISED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_ERROR(500);

    public final int code;

    HttpStatus(int code) {
        this.code = code;
    }
}
