package uk.nhs.digital.docstore.patientdetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
  void shouldCreateClientPatientDetailsDtoFromPatientDetails()
      throws IllFormedPatientDetailsException {
    var patientDetails =
        new PatientDetails(
            List.of(new PatientName("Jane")),
            new PatientName("Smith"),
            new BirthDate("2003-10-23"),
            new Postcode("PO1 2ST"),
            new NhsNumber("9123456780"));
    var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);
    assertEquals(clientPatientDetailsDto.getBirthDate(), patientDetails.getBirthDate().getValue());
    assertEquals(
        clientPatientDetailsDto.getFamilyName(), patientDetails.getFamilyName().getValue());
    assertEquals(clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber().getValue());
    assertEquals(
        clientPatientDetailsDto.getPostalCode(), patientDetails.getPostalCode().getValue());
    assertEquals(
        clientPatientDetailsDto.getGivenName().get(0),
        patientDetails.getGivenName().get(0).getValue());
  }

  @Test
  void shouldCreateClientPatientDetailsDtoFromPatientDetailsWithIncompleteInformation()
      throws IllFormedPatientDetailsException {
    var patientDetails = new PatientDetails(null, null, null, null, new NhsNumber("9123456780"));
    var clientPatientDetailsDto = ClientPatientDetailsDto.fromPatientDetails(patientDetails);
    assertNull(clientPatientDetailsDto.getBirthDate());
    assertNull(clientPatientDetailsDto.getFamilyName());
    assertEquals(clientPatientDetailsDto.getNhsNumber(), patientDetails.getNhsNumber().getValue());
    assertNull(clientPatientDetailsDto.getPostalCode());
    assertNull(clientPatientDetailsDto.getGivenName());
  }
}
