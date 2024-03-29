package uk.nhs.digital.docstore.data.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.List;

@DynamoDBTable(tableName = "DocumentReferenceMetadata")
@SuppressWarnings("unused")
public class DocumentMetadata {
    private String id;
    private String nhsNumber;
    private String location;
    private String contentType;
    private Boolean documentUploaded;
    private String fileName;
    private String created;
    private String indexed;
    private String deleted;
    private List<String> type;
    private String virusScanResult;

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

    @DynamoDBAttribute(attributeName = "FileName")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

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

    @DynamoDBAttribute(attributeName = "VirusScanResult")
    public String getVirusScanResult() {
        return virusScanResult;
    }

    public void setVirusScanResult(String virusScanResult) {
        this.virusScanResult = virusScanResult;
    }

    @Override
    public String toString() {
        return "DocumentMetadata{"
                + "id='"
                + id
                + '\''
                + ", nhsNumber='"
                + nhsNumber
                + '\''
                + ", location='"
                + location
                + '\''
                + ", contentType='"
                + contentType
                + '\''
                + ", documentUploaded="
                + documentUploaded
                + ", fileName='"
                + fileName
                + '\''
                + ", created='"
                + created
                + '\''
                + ", indexed='"
                + indexed
                + '\''
                + ", deleted='"
                + deleted
                + '\''
                + ", type="
                + type
                + '\''
                + ", virusScanResult="
                + virusScanResult
                + '}';
    }
}
