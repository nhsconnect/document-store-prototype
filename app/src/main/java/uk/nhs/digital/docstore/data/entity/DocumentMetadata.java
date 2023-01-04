package uk.nhs.digital.docstore.data.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import org.hl7.fhir.r4.model.Coding;
import uk.nhs.digital.docstore.NHSDocumentReference;

import java.util.List;

import static java.util.stream.Collectors.toList;

@DynamoDBTable(tableName = "DocumentReferenceMetadata")
@SuppressWarnings("unused")
public class DocumentMetadata {
    private String id;
    private String nhsNumber;
    private String location;
    private String contentType;
    private Boolean documentUploaded;
    private String description;
    private String created;
    private String indexed;
    private String deleted;
    private List<String> type;

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

    @DynamoDBAttribute(attributeName = "Created")
    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @DynamoDBAttribute(attributeName = "Indexed")
    public String getIndexed() {
        return indexed;
    }

    @DynamoDBAttribute(attributeName = "Deleted")
    public String getDeleted(){return deleted;}

    public void setDeleted(String deleted){this.deleted = deleted;}

    public void setIndexed(String indexed) {
        this.indexed = indexed;
    }

    @DynamoDBAttribute(attributeName = "Type")
    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public static DocumentMetadata from(NHSDocumentReference reference, String documentLocation) {
        var documentMetadata = new DocumentMetadata();
        documentMetadata.setNhsNumber(reference.getSubject().getIdentifier().getValue());
        documentMetadata.setContentType(reference.getContent().get(0).getAttachment().getContentType());
        documentMetadata.setLocation(documentLocation);
        documentMetadata.setDocumentUploaded(false);
        documentMetadata.setDescription(reference.getDescription());
        documentMetadata.setCreated(reference.getCreated().asStringValue());
        documentMetadata.setType(reference.getType().getCoding()
                .stream()
                .map(Coding::getCode)
                .collect(toList()));
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
                ", created='" + created + '\'' +
                ", indexed='" + indexed + '\'' +
                ", deleted='" + deleted + '\'' +
                ", type=" + type +
                '}';
    }
}
