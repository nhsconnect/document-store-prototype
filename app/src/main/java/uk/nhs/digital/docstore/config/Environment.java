package uk.nhs.digital.docstore.config;

public class Environment {
    public String getEnvVar(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }

    public String getEnvVar(String name) throws MissingEnvironmentVariableException {
        var value = System.getenv(name);
        if ("".equals(value)) {
            throw new MissingEnvironmentVariableException("The environment variable " + name + " is not set");
        }
        return value;
    }
}
