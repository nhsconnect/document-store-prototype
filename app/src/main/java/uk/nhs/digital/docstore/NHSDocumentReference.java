package uk.nhs.digital.docstore;

import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.util.ElementUtil;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Reference;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.model.Document;
import uk.nhs.digital.docstore.model.FileName;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.ScanResult;

@ResourceDef(
        name = "DocumentReference",
        profile =
                "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-DocumentReference-1")
public class NHSDocumentReference extends DocumentReference {

    private static final String DOCUMENT_TYPE_CODING_SYSTEM = "http://snomed.info/sct";


    @Child(name = "scanResult")
    private ScanResult scanResult;

    @Child(name = "created")
    @Description(
            shortDefinition =
                    "When the document was created. Creation/Edit datetime of the document - event"
                            + " date")
    private DateTimeType created;

    @Child(name = "indexed")
    @Description(shortDefinition = "When the document reference was created.")
    private InstantType indexed;

    @Child(name = "deleted")
    @Description(shortDefinition = "When the document reference was deleted.")
    private InstantType deleted;

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && ElementUtil.isEmpty(created, indexed);
    }

    public NHSDocumentReference setNhsNumber(NhsNumber nhsNumber) {
        setSubject(
                new Reference()
                        .setIdentifier(
                                new Identifier()
                                        .setSystem("https://fhir.nhs.uk/Id/nhs-number")
                                        .setValue(nhsNumber.getValue())));
        return this;
    }

    public NhsNumber getNhsNumber() throws IllFormedPatientDetailsException {
        return new NhsNumber(getSubject().getIdentifier().getValue());
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

    public NHSDocumentReference setIndexed(InstantType indexed) {
        this.indexed = indexed;
        return this;
    }

    public InstantType getDeleted() {
        if (deleted == null) {
            deleted = new InstantType();
        }
        return deleted;
    }

    public NHSDocumentReference setDeleted(InstantType deleted) {
        this.deleted = deleted;
        return this;
    }

    public String getContentType() {
        return getContentFirstRep().getAttachment().getContentType();
    }

    public FileName getFileName() {
        return new FileName(description.getValue());
    }

    public NHSDocumentReference setFileName(FileName fileName) {
        setDescription(fileName.getValue());
        return this;
    }

    public Document parse() throws IllFormedPatientDetailsException {
        return new Document(
                null,
                getNhsNumber(),
                getContentType(),
                indexed != null,
                getFileName(),
                created == null ? null : created.getValue().toInstant(),
                indexed == null ? null : indexed.getValue().toInstant(),
                deleted == null ? null : deleted.getValue().toInstant(),
                type.getCoding().stream()
                        .filter(coding -> coding.getSystem().equals(DOCUMENT_TYPE_CODING_SYSTEM))
                        .map(Coding::getCode)
                        .collect(Collectors.toList()),
                null,
                ScanResult.NOT_SCANNED);
    }
}
