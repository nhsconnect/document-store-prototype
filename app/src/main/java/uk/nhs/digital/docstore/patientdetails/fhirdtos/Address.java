package uk.nhs.digital.docstore.patientdetails.fhirdtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
    private Period period;
    private String postalCode;
    private String use;

    public Address(@JsonProperty("period")Period period,
                   @JsonProperty("postalCode")String postalCode,
                   @JsonProperty("use")String use) {
        this.period = period;
        this.postalCode = postalCode;
        this.use = use;
    }

    public Period getPeriod() {
        return period;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getUse() {
        return use;
    }
}
