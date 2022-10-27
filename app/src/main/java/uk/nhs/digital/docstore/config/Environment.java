package uk.nhs.digital.docstore.config;

public class Environment {
    public String getEnvVar(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
