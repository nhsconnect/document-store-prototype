package uk.nhs.digital.docstore.utils;

import uk.nhs.digital.docstore.NHSNumberSearchParameterForm;
import uk.nhs.digital.docstore.exceptions.MissingSearchParametersException;

import java.util.Map;
import java.util.UUID;

public class CommonUtils {

    public static String generateRandomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public String getNhsNumberFrom(Map<String, String> queryParameters) {
        if (queryParameters == null) {
            throw new MissingSearchParametersException("subject:identifier");
        }
        NHSNumberSearchParameterForm nhsNumberSearchParameterForm = new NHSNumberSearchParameterForm(queryParameters);
        return nhsNumberSearchParameterForm.getNhsNumber();
    }
}
