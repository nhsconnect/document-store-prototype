package uk.nhs.digital.docstore.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EnvironmentTest {

  private Environment environment = new Environment();

  @Test
  void provideEnvironmentVariableValueIfEnvironmentVariableExists() {
    var env = System.getenv();
    var firstKey = env.keySet().stream().findFirst();
    if (firstKey.isEmpty()) {
      throw new RuntimeException("No env vars set");
    }

    String returnedValue = environment.getEnvVar(firstKey.get(), "a default");

    assertThat(returnedValue).isEqualTo(env.get(firstKey.get()));
  }

  @Test
  void provideDefaultIfEnvironmentVariableDoesNotExist() {
    String returnedValue = environment.getEnvVar("NON_EXISTENT_ENV_VAR", "the default");

    assertThat(returnedValue).isEqualTo("the default");
  }
}
