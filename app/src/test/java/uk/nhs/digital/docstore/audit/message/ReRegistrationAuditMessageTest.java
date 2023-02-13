package uk.nhs.digital.docstore.audit.message;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.nhs.digital.docstore.events.ReRegistrationEvent;
import uk.nhs.digital.docstore.exceptions.IllFormedPatientDetailsException;
import uk.nhs.digital.docstore.helpers.DocumentBuilder;

class ReRegistrationAuditMessageTest {

    @Test
    void getDescriptionWhenNoDocumentsFound() throws IllFormedPatientDetailsException {
        var reRegistrationEvent = new ReRegistrationEvent("1234567891", "some-nems-message-id");

        var reRegistrationAuditMessage =
                new ReRegistrationAuditMessage(reRegistrationEvent, Collections.emptyList());

        assertThat(reRegistrationAuditMessage.getDescription())
                .isEqualTo("Found no documents for re-registered patient");
    }

    @Test
    void getDescriptionWhenDocumentsFoundAndDeleted() throws IllFormedPatientDetailsException {
        var reRegistrationEvent = new ReRegistrationEvent("1234567891", "some-nems-message-id");
        var documents = List.of(DocumentBuilder.baseDocumentBuilder().build());

        var reRegistrationAuditMessage =
                new ReRegistrationAuditMessage(reRegistrationEvent, documents);

        assertThat(reRegistrationAuditMessage.getDescription())
                .isEqualTo("Deleted documents for re-registered patient");
    }
}
