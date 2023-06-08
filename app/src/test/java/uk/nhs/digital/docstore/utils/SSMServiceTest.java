package uk.nhs.digital.docstore.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SSMServiceTest {

    @Test
    void retrieveValueFromParameterStore() {
        var mockSsm = Mockito.mock(AWSSimpleSystemsManagement.class);
        var param = "param";
        var value = "token";

        when(mockSsm.getParameter(any()))
                .thenReturn(
                        new GetParameterResult().withParameter(new Parameter().withValue(value)));

        var mockSSMService = new SSMService(mockSsm);
        assertThat(mockSSMService.retrieveParameterStoreValue(param)).isEqualTo(value);
    }
}
