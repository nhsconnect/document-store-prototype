package uk.nhs.digital.docstore.patientdetails;

import uk.nhs.digital.docstore.exceptions.PatientNotFoundException;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Address;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Name;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Patient;
import uk.nhs.digital.docstore.patientdetails.fhirdtos.Period;

import java.time.LocalDate;
import java.util.List;

public class FakePdsFhirService implements PdsFhirService {
    public Patient fetchPatientDetails(String nhsNumber) {
        var currentPeriod = new Period(LocalDate.now().minusYears(1), null);
        switch (nhsNumber) {
            case "9000000025":
                var restrictedPatientName = new Name(currentPeriod, "usual", List.of("Janet"), "Smythe");
                return new Patient(nhsNumber, "2010-10-22", null, List.of(restrictedPatientName));
            case "9111231130":
                throw new PatientNotFoundException("Patient does not exist for given NHS number.");
            default:
                var defaultAddress = new Address(currentPeriod, "LS1 6AE", "home");
                var defaultName = new Name(currentPeriod, "usual", List.of("Jane"), "Smith");
                return new Patient(nhsNumber, "2010-10-22", List.of(defaultAddress), List.of(defaultName));
        }
    }
}
