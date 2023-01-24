package uk.nhs.digital.docstore.patientdetails;

import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;
import uk.nhs.digital.docstore.model.Postcode;

import java.util.List;

public class ClientPatientDetailsDto {
    private final List<String> givenName;
    private final String familyName;
    private final String birthDate;
    private final Postcode postalCode;
    private final NhsNumber nhsNumber;

    public ClientPatientDetailsDto(List<String> givenName, String familyName, String birthDate, Postcode postalCode,
                                   NhsNumber nhsNumber) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthDate = birthDate;
        this.postalCode = postalCode;
        this.nhsNumber = nhsNumber;
    }

    public List<String> getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getPostalCode() {
        return postalCode == null ? null : postalCode.getValue();
    }

    public String getNhsNumber() {
        return nhsNumber.getValue();
    }

    public static ClientPatientDetailsDto fromPatientDetails(PatientDetails patientDetails) {
        return new ClientPatientDetailsDto(
                patientDetails.getGivenName(),
                patientDetails.getFamilyName(),
                patientDetails.getBirthDate(),
                patientDetails.getPostalCode(),
                patientDetails.getNhsNumber()
        );
    }
}
