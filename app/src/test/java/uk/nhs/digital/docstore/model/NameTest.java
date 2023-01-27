package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NameTest {
    @Test
    void redactsNameExceptForFirstChar() {
        assertEquals("J***", new Name("John").toString());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenRedactingNullName() {
        assertThrows(NullPointerException.class, () -> new Name(null).toString());
    }
}
