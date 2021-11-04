package uk.nhs.digital.docstore;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.InstantType;

import java.time.Instant;

@ResourceDef(name="DocumentReference", profile="https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1")
public class NHSDocumentReference extends DocumentReference {

    @Child(name="created")
    @Description(shortDefinition="When the document was created. Creation/Edit datetime of the document - event date")
    private DateTimeType created;

    @Child(name="indexed")
    @Description(shortDefinition="When the document reference was created.")
    private InstantType indexed;

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(created, indexed);
    }

    public DateTimeType getCreated() {
        if (created == null) {
            created = new DateTimeType();
        }
        return created;
    }

    public NHSDocumentReference setCreated(DateTimeType created) {
        this.created = created;
        return this;
    }

    public InstantType getIndexed() {
        if (indexed == null) {
            indexed = new InstantType();
        }
        return indexed;
    }

    public NHSDocumentReference setIndexed(InstantType indexed) {
        this.indexed = indexed;
        return this;
    }
}
