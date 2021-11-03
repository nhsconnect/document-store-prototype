package uk.nhs.digital.docstore;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import org.hl7.fhir.r4.model.DocumentReference;

@DynamoDBTable(tableName = "DocumentReferenceMetadata")
@SuppressWarnings("unused")
public class DocumentMetadata {
    private String id;
    private String nhsNumber;
    private String location;
    private String contentType;
    private Boolean documentUploaded;
    private String description;

    @DynamoDBHashKey(attributeName = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "NhsNumber")
    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    @DynamoDBIndexHashKey(attributeName = "Location", globalSecondaryIndexName = "LocationsIndex")
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @DynamoDBAttribute(attributeName = "ContentType")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @DynamoDBAttribute(attributeName = "DocumentUploaded")
    public Boolean isDocumentUploaded() {
        return documentUploaded;
    }

    public void setDocumentUploaded(Boolean documentUploaded) {
        this.documentUploaded = documentUploaded;
    }

    @DynamoDBAttribute(attributeName = "Description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static DocumentMetadata from(DocumentReference reference, DocumentStore.DocumentDescriptor documentDescriptor) {
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setNhsNumber(reference.getSubject().getIdentifier().getValue());
        documentMetadata.setContentType(reference.getContent().get(0).getAttachment().getContentType());
        documentMetadata.setLocation(documentDescriptor.toLocation());
        documentMetadata.setDocumentUploaded(false);
        documentMetadata.setDescription(reference.getDescription());
        return documentMetadata;
    }

    @Override
    public String toString() {
        return "DocumentMetadata{" +
                "id='" + id + '\'' +
                ", nhsNumber='" + nhsNumber + '\'' +
                ", location='" + location + '\'' +
                ", contentType='" + contentType + '\'' +
                ", documentUploaded=" + documentUploaded +
                ", description='" + description + '\'' +
                '}';
    }
}
