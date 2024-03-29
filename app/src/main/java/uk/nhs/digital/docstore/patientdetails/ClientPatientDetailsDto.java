package uk.nhs.digital.docstore.patientdetails;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import uk.nhs.digital.docstore.model.*;

public class ClientPatientDetailsDto {
    private final List<PatientName> givenName;
    private final PatientName familyName;
    private final BirthDate birthDate;
    private final Postcode postalCode;
    private final NhsNumber nhsNumber;
    private final boolean superseded;
    private final boolean restricted;

    public ClientPatientDetailsDto(
            List<PatientName> givenName,
            PatientName familyName,
            BirthDate birthDate,
            Postcode postalCode,
            NhsNumber nhsNumber,
            boolean superseded,
            boolean restricted) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthDate = birthDate;
        this.postalCode = postalCode;
        this.nhsNumber = nhsNumber;
        this.superseded = superseded;
        this.restricted = restricted;
    }

    public List<String> getGivenName() {
        return givenName.stream().map(PatientName::getValue).collect(Collectors.toList());
    }

    public String getFamilyName() {
        return familyName.getValue();
    }

    public String getBirthDate() {
        return birthDate.getValue();
    }

    public String getPostalCode() {
        return postalCode.getValue();
    }

    public String getNhsNumber() {
        return nhsNumber.getValue();
    }

    public boolean isSuperseded() {
        return superseded;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public static ClientPatientDetailsDto fromPatientDetails(PatientDetails patientDetails) {
        return new ClientPatientDetailsDto(
                patientDetails.getGivenName().orElse(Collections.emptyList()),
                patientDetails.getFamilyName().orElse(new PatientName("")),
                patientDetails.getBirthDate().orElse(new BirthDate("")),
                patientDetails.getPostalCode().orElse(new Postcode("")),
                patientDetails.getNhsNumber(),
                patientDetails.isSuperseded(),
                patientDetails.isRestricted());
    }
}
