package uk.nhs.digital.docstore.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTest {

    private Environment environment = new Environment();

    @Test
    void provideEnvironmentVariableValueIfEnvironmentVariableExists() {
        var alwaysExistentEnvVar = "USER";
        var valueFromEnvironment = System.getenv(alwaysExistentEnvVar);

        String returnedValue = environment.getEnvVar(alwaysExistentEnvVar, "a default");

        assertThat(returnedValue).isEqualTo(valueFromEnvironment);
    }

    @Test
    void provideDefaultIfEnvironmentVariableDoesNotExist() {
        String returnedValue = environment.getEnvVar("NON_EXISTENT_ENV_VAR", "the default");

        assertThat(returnedValue).isEqualTo("the default");
    }
}