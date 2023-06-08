package uk.nhs.digital.docstore.patientdetails;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.nhs.digital.docstore.config.Environment;
import uk.nhs.digital.docstore.utils.SSMService;

class PatientSearchConfigTest {

    @Test
    void shouldTurnOffPdsFhirStubbingIfEnvironmentVarSetToFalse() {
        var stubEnvironment = new StubEnvironment().withVariable("PDS_FHIR_IS_STUBBED", "false");
        var mockSsm = Mockito.mock(SSMService.class);

        var config = new PatientSearchConfig(stubEnvironment, mockSsm);

        assertThat(config.pdsFhirIsStubbed()).isFalse();
    }

    @Test
    void shouldTurnOnPdsFhirStubbingIfEnvironmentVarIsNotSet() {
        var stubEnvironment = new StubEnvironment().withoutVariable("PDS_FHIR_IS_STUBBED");
        var mockSsm = Mockito.mock(SSMService.class);

        var config = new PatientSearchConfig(stubEnvironment, mockSsm);

        assertThat(config.pdsFhirIsStubbed()).isTrue();
    }

    @Test
    void shouldTurnOnPdsFhirStubbingIfEnvironmentVarIsSetToTrue() {
        var stubEnvironment = new StubEnvironment().withVariable("PDS_FHIR_IS_STUBBED", "true");
        var mockSsm = Mockito.mock(SSMService.class);

        var config = new PatientSearchConfig(stubEnvironment, mockSsm);

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
