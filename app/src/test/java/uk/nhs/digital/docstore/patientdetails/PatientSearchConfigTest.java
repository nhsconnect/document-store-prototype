package uk.nhs.digital.docstore.patientdetails;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.config.Environment;

class PatientSearchConfigTest {

  @Test
  void shouldTurnOffPdsFhirStubbingIfEnvironmentVarSetToFalse() {
    var stubEnvironment = new StubEnvironment().withVariable("PDS_FHIR_IS_STUBBED", "false");

    var config = new PatientSearchConfig(stubEnvironment);

    assertThat(config.pdsFhirIsStubbed()).isFalse();
  }

  @Test
  void shouldTurnOnPdsFhirStubbingIfEnvironmentVarIsNotSet() {
    var stubEnvironment = new StubEnvironment().withoutVariable("PDS_FHIR_IS_STUBBED");

    var config = new PatientSearchConfig(stubEnvironment);

    assertThat(config.pdsFhirIsStubbed()).isTrue();
  }

  @Test
  void shouldTurnOnPdsFhirStubbingIfEnvironmentVarIsSetToTrue() {
    var stubEnvironment = new StubEnvironment().withVariable("PDS_FHIR_IS_STUBBED", "true");

    var config = new PatientSearchConfig(stubEnvironment);

    assertThat(config.pdsFhirIsStubbed()).isTrue();
  }

  private class StubEnvironment extends Environment {

    private final HashMap<String, String> vars = new HashMap<>();

    @Override
    public String getEnvVar(String name, String defaultValue) {
      return vars.getOrDefault(name, defaultValue);
    }

    public StubEnvironment withVariable(String variableName, String value) {
      vars.put(variableName, value);
      return this;
    }

    public StubEnvironment withoutVariable(String variableName) {
      vars.remove(variableName);
      return this;
    }
  }
}
