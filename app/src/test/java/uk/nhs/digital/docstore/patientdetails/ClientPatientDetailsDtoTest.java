package uk.nhs.digital.docstore.patientdetails;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.BirthDate;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;
import uk.nhs.digital.docstore.model.PatientName;
import uk.nhs.digital.docstore.model.Postcode;

class ClientPatientDetailsDtoTest {

    @Test
    void createsClientPatientDetailsDtoFromPatientDetails()
            throws IllFormedPatientDetailsException {
        var patientDetails =
                new PatientDetails(
                        List.of(new PatientName("Jane")),
                        new PatientName("Smith"),
                        new BirthDate("2003-10-23"),
                        new Postcode("PO1 2ST"),
                        new NhsNumber("9123456780"),
                        true);

        var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);

        assertTrue(patientDetails.getBirthDate().isPresent());
        assertEquals(
                clientPatientDetailsDto.getBirthDate(),
                patientDetails.getBirthDate().get().getValue());
        assertTrue(patientDetails.getGivenName().isPresent());
        assertEquals(
                clientPatientDetailsDto.getGivenName().get(0),
                patientDetails.getGivenName().get().get(0).getValue());
        assertTrue(patientDetails.getFamilyName().isPresent());
        assertEquals(
                clientPatientDetailsDto.getFamilyName(),
                patientDetails.getFamilyName().get().getValue());
        assertTrue(patientDetails.getPostalCode().isPresent());
        assertEquals(
                clientPatientDetailsDto.getPostalCode(),
                patientDetails.getPostalCode().get().getValue());
        assertEquals(
                clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber().getValue());
        assertEquals(clientPatientDetailsDto.isSuperseded(), patientDetails.isSuperseded());
    }

    @Test
    void createClientPatientDetailsDtoFromPatientDetailsWithIncompleteInfo()
            throws IllFormedPatientDetailsException {
        var patientDetails =
                new PatientDetails(null, null, null, null, new NhsNumber("9123456780"), false);

        var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);

        assertEquals(clientPatientDetailsDto.getBirthDate(), "");
        assertEquals(clientPatientDetailsDto.getFamilyName(), "");
        assertEquals(clientPatientDetailsDto.getPostalCode(), "");
        assertEquals(clientPatientDetailsDto.getGivenName(), Collections.emptyList());
        assertEquals(
                clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber().getValue());
        assertEquals(clientPatientDetailsDto.isSuperseded(), patientDetails.isSuperseded());
    }
}
