package uk.nhs.digital.docstore.patientdetails;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;
import uk.nhs.digital.docstore.model.Postcode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientPatientDetailsDtoTest {

    @Test
    void shouldCreateClientPatientDetailsDtoFromPatientDetails() throws IllFormedPatientDetailsException {
        var patientDetails = new PatientDetails(List.of("Jane"), "Smith", "2003-10-23", new Postcode("PO1 2ST"), new NhsNumber("9123456780"));
        var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);
        assertEquals(clientPatientDetailsDto.getBirthDate(), patientDetails.getBirthDate());
        assertEquals(clientPatientDetailsDto.getFamilyName(), patientDetails.getFamilyName());
        assertEquals(clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber().getValue());
        assertEquals(clientPatientDetailsDto.getPostalCode(), patientDetails.getPostalCode().getValue());
        assertEquals(clientPatientDetailsDto.getGivenName(), patientDetails.getGivenName());
    }

    @Test
    void shouldCreateClientPatientDetailsDtoFromPatientDetailsWithIncompleteInformation() throws IllFormedPatientDetailsException {
        var patientDetails = new PatientDetails(null, null, "2003-10-23", null, new NhsNumber("9123456780"));
        var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);
        assertEquals(clientPatientDetailsDto.getBirthDate(), patientDetails.getBirthDate());
        assertEquals(clientPatientDetailsDto.getFamilyName(), patientDetails.getFamilyName());
        assertEquals(clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber().getValue());
        assertNull(clientPatientDetailsDto.getPostalCode());
        assertEquals(clientPatientDetailsDto.getGivenName(), patientDetails.getGivenName());
    }

}