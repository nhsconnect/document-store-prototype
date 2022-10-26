package uk.nhs.digital.docstore.config;

public class StubbedApiConfig extends ApiConfig {
    private String amplifyBaseUrl;

    public StubbedApiConfig(String amplifyBaseUrl) {
        this.amplifyBaseUrl = amplifyBaseUrl;
    }

    @Override
    public String getAmplifyBaseUrl() {
        return amplifyBaseUrl;
    }
}
