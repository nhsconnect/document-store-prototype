package uk.nhs.digital.docstore;

import ca.uhn.fhir.context.FhirContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.hl7.fhir.r4.model.*;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import static org.hl7.fhir.r4.model.Bundle.BundleType.SEARCHSET;

public class RetrievePatientDetailsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>  {
    private static final String NHS_NUMBER_SYSTEM_ID = "https://fhir.nhs.uk/Id/nhs-number";
    private final FhirContext fhirContext;

    public RetrievePatientDetailsHandler() {
        this.fhirContext = FhirContext.forR4();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        var jsonParser = fhirContext.newJsonParser();

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
                .setValue("12345")
                .setExtension(List.of(identifierExtension));

        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.USUAL).setFamily("Doe").setGiven(List.of(new StringType("Jane")));
        LocalDate birthdate = new LocalDate(1998, 7, 11);

        Patient patient = new Patient();
        patient.setIdentifier(List.of(patientIdentifier));
        patient.addAddress()
                    .setPostalCode("LS1 6AE")
                    .setUse(Address.AddressUse.HOME)
                    .addExtension()
                        .setUrl("https://fhir.hl7.org.uk/StructureDefinition/Extension-UKCore-AddressKey")
                        .addExtension(addressTypeExtension).addExtension(addressValueExtension);
        patient.setId("1234567890");
        patient.setName(List.of(name));
        patient.setBirthDate(birthdate.toDate());

        Bundle bundle = new Bundle();
        Bundle.BundleEntryComponent bundleEntryComponent = new Bundle.BundleEntryComponent().setResource(patient);
        bundle.setTotal(1)
                .setType(SEARCHSET)
                .setEntry(List.of(bundleEntryComponent));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", "application/fhir+json"))
                .withBody(jsonParser.encodeResourceToString(bundle));
    }
}
