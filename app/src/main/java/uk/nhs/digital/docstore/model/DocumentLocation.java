package uk.nhs.digital.docstore.model;

import java.net.URI;

public class DocumentLocation {
  private final URI location;

  public DocumentLocation(String location) {
    this.location = URI.create(location);
  }

  public String getBucketName() {
    return location.getHost();
  }

  public String getPath() {
    return location.getPath().substring(1);
  }

  @Override
  public String toString() {
    return location.toString();
  }
}
