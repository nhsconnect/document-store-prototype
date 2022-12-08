package uk.nhs.digital.docstore.patientdetails;

import org.hl7.fhir.r4.model.*;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;

public class BundleMapper {
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";

    public Bundle toBundle(List<PatientDetails> patientDetailsList) {
        var entries = patientDetailsList.stream()
                .map(this::toBundleEntry)
                .collect(toList());

        return new Bundle()
                .setTotal(entries.size())
                .setType(SEARCHSET)
                .setEntry(entries);
    }

    private Bundle.BundleEntryComponent toBundleEntry(PatientDetails patientDetails) {
        return new Bundle.BundleEntryComponent()
                .setResource(toFhirPatient(patientDetails));
    }

    private Patient toFhirPatient(PatientDetails patientDetails){
        Extension addressTypeExtension = new Extension();
        Coding addressCoding = new Coding();
        addressCoding.setSystem("https://fhir.hl7.org.uk/CodeSystem/UKCore-AddressKeyType").setCode("PAF");
        addressTypeExtension.setUrl("type").setValue(addressCoding);
        Extension addressValueExtension = new Extension();
        addressValueExtension.setUrl("value").setValue(new StringType( "12345678"));

        Identifier patientIdentifier = new Identifier();
        Extension identifierExtension = new Extension();
        CodeableConcept identifierCodeableConcept = new CodeableConcept();
        Coding identifierCoding = new Coding();
        identifierCoding
                .setSystem("https://fhir.hl7.org.uk/CodeSystem/UKCore-NHSNumberVerificationStatus")
                .setCode("01")
                .setVersion("1.0.0")
                .setDisplay("Number present and verified");
        identifierExtension
                .setUrl("https://fhir.hl7.org.uk/StructureDefinition/Extension-UKCore-NHSNumberVerificationStatus")
                .setValue(identifierCodeableConcept.setCoding(List.of(identifierCoding)));
        patientIdentifier
                .setSystem(NHS_NUMBER_SYSTEM_ID)
                .setValue(patientDetails.getNhsNumber())
                .setExtension(List.of(identifierExtension));

        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.USUAL)
                .setFamily(patientDetails.getFamilyName())
                .setGiven(patientDetails.getGivenName().stream().map(StringType::new).collect(toList()));
        LocalDate birthdate = new LocalDate(patientDetails.getBirthDate());

        Patient patient = new Patient();
        patient.setIdentifier(List.of(patientIdentifier));
        patient.addAddress()
                .setPostalCode(patientDetails.getPostalCode())
                .setUse(Address.AddressUse.HOME)
                .addExtension()
                .setUrl("https://fhir.hl7.org.uk/StructureDefinition/Extension-UKCore-AddressKey")
                .addExtension(addressTypeExtension).addExtension(addressValueExtension);
        patient.setId(patientDetails.getNhsNumber());
        patient.setName(List.of(name));
        patient.setBirthDate(birthdate.toDate());

        return patient;
    }
}
