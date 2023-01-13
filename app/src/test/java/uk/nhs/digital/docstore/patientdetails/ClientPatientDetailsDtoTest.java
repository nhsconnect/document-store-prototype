package uk.nhs.digital.docstore.patientdetails;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.model.PatientDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientPatientDetailsDtoTest {

    @Test
    void shouldCreateClientPatientDetailsDtoFromPatientDetails() {
        var patientDetails = new PatientDetails(List.of("Jane"), "Smith", "2003-10-23", "PO1 2ST", "9123456780");
        var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);
        assertEquals(clientPatientDetailsDto.getBirthDate(), patientDetails.getBirthDate());
        assertEquals(clientPatientDetailsDto.getFamilyName(), patientDetails.getFamilyName());
        assertEquals(clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber());
        assertEquals(clientPatientDetailsDto.getPostalCode(), patientDetails.getPostalCode());
        assertEquals(clientPatientDetailsDto.getGivenName(), patientDetails.getGivenName());
    }

    @Test
    void shouldCreateClientPatientDetailsDtoFromPatientDetailsWithIncompleteInformation() {
        var patientDetails = new PatientDetails(null, null, "2003-10-23", null, "9123456780");
        var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);
        assertEquals(clientPatientDetailsDto.getBirthDate(), patientDetails.getBirthDate());
        assertEquals(clientPatientDetailsDto.getFamilyName(), patientDetails.getFamilyName());
        assertEquals(clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber());
        assertEquals(clientPatientDetailsDto.getPostalCode(), patientDetails.getPostalCode());
        assertEquals(clientPatientDetailsDto.getGivenName(), patientDetails.getGivenName());
    }

}