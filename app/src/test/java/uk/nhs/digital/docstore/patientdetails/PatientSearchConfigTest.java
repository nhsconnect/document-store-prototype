package uk.nhs.digital.docstore.patientdetails;

import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.config.Environment;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class PatientSearchConfigTest {

    @Test
    void shouldTurnOffPdsAdaptorStubbingIfEnvironmentVarSetToFalse() {
        var stubEnvironment = new StubEnvironment().withVariable("PDS_ADAPTOR_IS_STUBBED", "false");

        var config = new PatientSearchConfig(stubEnvironment);

        assertThat(config.pdsAdaptorIsStubbed()).isFalse();
    }

    @Test
    void shouldTurnOnPdsAdaptorStubbingIfEnvironmentVarIsNotSet() {
        var stubEnvironment = new StubEnvironment().withoutVariable("PDS_ADAPTOR_IS_STUBBED");

        var config = new PatientSearchConfig(stubEnvironment);

        assertThat(config.pdsAdaptorIsStubbed()).isTrue();
    }

    @Test
    void shouldTurnOnPdsAdaptorStubbingIfEnvironmentVarIsSetToTrue() {
        var stubEnvironment = new StubEnvironment().withVariable("PDS_ADAPTOR_IS_STUBBED", "true");

        var config = new PatientSearchConfig(stubEnvironment);

        assertThat(config.pdsAdaptorIsStubbed()).isTrue();
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