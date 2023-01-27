package uk.nhs.digital.docstore.model;

import java.util.Arrays;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BirthDate {
  private final String value;

  public BirthDate(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

  @Override
  public boolean equals(Object other) {
    return EqualsBuilder.reflectionEquals(this, other);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    var birthDateArray = Arrays.asList(this.value.split("-"));
    birthDateArray.set(0, "****");
    return String.join("-", birthDateArray);
  }
}
