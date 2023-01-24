package uk.nhs.digital.docstore.patientdetails.fhirdtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.digital.docstore.exceptions.IllFormedPatentDetailsException;
import uk.nhs.digital.docstore.model.NhsNumber;
import uk.nhs.digital.docstore.model.PatientDetails;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {
    private String id;
    private String birthDate;
    private List<Address> addresses;
    private List<Name> names;

    private static final Logger logger = LoggerFactory.getLogger(Patient.class);

    public Patient(@JsonProperty("id") String id,
                   @JsonProperty("birthDate")String birthDate,
                   @JsonProperty("address")List<Address> addresses,
                   @JsonProperty("name")List<Name> names) {
        this.id = id;
        this.birthDate = birthDate;
        this.addresses = addresses;
        this.names = names;
    }

    public String getId() {
        return id;
    }

    public String getBirthDate() {
        return birthDate;
    }

    private List<Address> getAddresses() {
        return addresses;
    }

    private List<Name> getNames() {
        return names;
    }

    public Optional<Name> getCurrentUsualName() {
        if (getNames() == null || getNames().size() == 0) {
            logger.warn("PDS-FHIR response has no 'name' for the patient");
            return Optional.empty();
        }

        var nameOfTypeUsual = getNames().stream().filter(name ->
                name.getUse().equalsIgnoreCase("Usual") && name.getPeriod().isCurrent()
        ).findFirst();

        if (nameOfTypeUsual.isEmpty()){
            logger.warn("PDS-FHIR response has no current 'name' of type 'usual' for the patient");
        }
        return nameOfTypeUsual;
    }

    public Optional<Address> getCurrentHomeAddress() {
        var addresses = getAddresses();

        if (addresses == null) {
            return Optional.empty();
        }

        Optional<Address> addressOfTypeHome = addresses.stream().filter(
                (address) -> Objects.equals(address.getUse(), "home") && address.getPeriod().isCurrent()
        ).findFirst();
        if (addressOfTypeHome.isEmpty()) {
            logger.warn("PDS-FHIR response has no current 'address' of type 'home' for the patient");
        }
        return addressOfTypeHome;
    }

    public static Patient parseFromJson(String json){
        var objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        try {
            return objectMapper.readValue(json, Patient.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public PatientDetails parse() throws IllFormedPatentDetailsException {
        var name = this.getCurrentUsualName();
        var address = this.getCurrentHomeAddress();
        return new PatientDetails(
                name.map(Name::getGiven).orElse(null),
                name.map(Name::getFamily).orElse(null),
                this.getBirthDate(),
                address.map(Address::getPostalCode).orElse(null),
                new NhsNumber(this.getId())
        );
    }
}
