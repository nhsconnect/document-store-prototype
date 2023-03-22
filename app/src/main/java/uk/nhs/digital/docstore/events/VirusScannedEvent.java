package uk.nhs.digital.docstore.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusScannedEvent {
    private final String bucketName;
    private final String key;
    private final String result;

    public VirusScannedEvent(
            @JsonProperty(value = "bucketName", required = true) String bucketName,
            @JsonProperty(value = "key", required = true) String key,
            @JsonProperty(value = "result", required = true) String result) {
        this.bucketName = bucketName;
        this.key = key;
        this.result = result;
    }

    public static VirusScannedEvent parse(String json) throws JsonProcessingException {
        var objectMapper = JsonMapper.builder().build();
        return objectMapper.readValue(json, VirusScannedEvent.class);
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getKey() {
        return key;
    }

    public String getResult() {
        return result;
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
