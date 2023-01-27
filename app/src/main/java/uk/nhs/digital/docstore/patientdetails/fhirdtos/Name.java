package uk.nhs.digital.docstore.patientdetails.fhirdtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Name {
  private Period period;
  private String use;
  private List<String> given;
  private String family;

  public Name(
      @JsonProperty("period") Period period,
      @JsonProperty("use") String use,
      @JsonProperty("given") List<String> given,
      @JsonProperty("family") String family) {
    this.period = period;
    this.use = use;
    this.given = given;
    this.family = family;
  }

  public Period getPeriod() {
    return period;
  }

  public String getUse() {
    return use;
  }

  public List<String> getGiven() {
    return given;
  }

  public String getFamily() {
    return family;
  }
}
