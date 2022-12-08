package uk.nhs.digital.docstore.patientdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientDetails {
    private final List<String> givenName;
    private final String familyName;
    private final String birthDate;
    private final String postalCode;
    private final String nhsNumber;

    public PatientDetails(List<String> givenName, String familyName, String birthDate, String postalCode,
                          String nhsNumber) {
        this.givenName = givenName;
        this.familyName = familyName;
        this.birthDate = birthDate;
        this.postalCode = postalCode;
        this.nhsNumber = nhsNumber;
    }

    public PatientDetails(@JsonProperty("name") List<Name> name,
                          @JsonProperty("birthDate") String birthDate,
                          @JsonProperty("address") List<Address> address,
                          @JsonProperty("id") String nhsNumber) {

        this.givenName = getGivenNameFromName(name);
        this.familyName = getFamilyNameFromName(name);
        this.birthDate = birthDate;
        this.postalCode = getPostalCodeFromAddress(address);
        this.nhsNumber = nhsNumber;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {
        private final List<String> given;
        private final String family;

        public Name(@JsonProperty("given") List<String> given, @JsonProperty("family") String family){
            this.given = given;
            this.family = family;
        }

        public List<String> getGiven() {
            return given;
        }

        public String getFamily() {
            return family;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private final String postalCode;

        public Address(@JsonProperty("postalCode") String postalCode){
            this.postalCode = postalCode;
        }

        public String getPostalCode() {
            return postalCode;
        }
    }

    public List<String> getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    private String getPostalCodeFromAddress(List<Address> address) {
        if (address == null){
            return null;
        }
        return address.get(0).getPostalCode();
    }

    private List<String> getGivenNameFromName(List<Name> name) {
        if (name == null){
            return null;
        }
        return name.get(0).getGiven();
    }

    private String getFamilyNameFromName(List<Name> name) {
        if (name == null){
            return null;
        }
        return name.get(0).getFamily();
    }
}
