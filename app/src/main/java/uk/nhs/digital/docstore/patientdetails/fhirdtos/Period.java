package uk.nhs.digital.docstore.patientdetails.fhirdtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Period {
  private LocalDate start;
  private LocalDate end;

  public Period(@JsonProperty("start") LocalDate start, @JsonProperty("end") LocalDate end) {
    this.start = start;
    this.end = end;
  }

  public boolean isCurrent() {
    return getStart().isBefore(LocalDate.now())
        && (getEnd() == null || getEnd().isAfter(LocalDate.now()));
  }

  private LocalDate getEnd() {
    return end;
  }

  private LocalDate getStart() {
    return start;
  }
}
