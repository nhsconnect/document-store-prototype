package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.util.List;
import uk.nhs.digital.docstore.audit.message.SearchPatientDetailsAuditMessage;
import uk.nhs.digital.docstore.audit.publisher.AuditPublisher;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Address;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Name;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Period;

public class FakePdsFhirService implements PdsFhirService {
    private final AuditPublisher sensitiveIndex;

    public FakePdsFhirService(AuditPublisher sensitiveIndex) {
        this.sensitiveIndex = sensitiveIndex;
    }

    public PatientDetails fetchPatientDetails(NhsNumber nhsNumber)
            throws JsonProcessingException, IllFormedPatientDetailsException {
        var currentPeriod = new Period(LocalDate.now().minusYears(1), null);
        var nhsNumberValue = nhsNumber.getValue();
        switch (nhsNumberValue) {
            case "9000000025":
                var restrictedPatientName =
                        new Name(currentPeriod, "usual", List.of("Janet"), "Smythe");

                sensitiveIndex.publish(new SearchPatientDetailsAuditMessage(nhsNumber, 200));

                return new Patient(
                                nhsNumberValue, "2010-10-22", null, List.of(restrictedPatientName))
                        .parse();
            case "9111231130":
                sensitiveIndex.publish(new SearchPatientDetailsAuditMessage(nhsNumber, 500));

                throw new PatientNotFoundException("Patient does not exist for given NHS number.");
            default:
                var defaultAddress = new Address(currentPeriod, "LS1 6AE", "home");
                var defaultName = new Name(currentPeriod, "usual", List.of("Jane"), "Smith");

                sensitiveIndex.publish(new SearchPatientDetailsAuditMessage(nhsNumber, 200));

                return new Patient(
                                nhsNumberValue,
                                "2010-10-22",
                                List.of(defaultAddress),
                                List.of(defaultName))
                        .parse();
        }
    }
}
