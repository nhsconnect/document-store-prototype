package uk.nhs.digital.docstore.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FileNameTest {
    @Test
    void redactsEverythingExceptFirstAndLastCharOfFileNameAndType() {
        var value = "long-test-name.pdf";
        var fileName = new FileName(value);

        var redactedFileName = fileName.toString();

        assertEquals(redactedFileName, "l***e.pdf");
    }

    @Test
    void redactsFileNameExceptFirstAndLastCharIfThereIsNoFileType() {
        var value = "long-test-name";
        var fileName = new FileName(value);

        var redactedFileName = fileName.toString();

        assertEquals("l***e", redactedFileName);
    }

    @Test
    void redactsEverythingExceptFirstAndLastCharOfFileNameAndAllFileTypes() {
        var value = "test.cy.js";
        var fileName = new FileName(value);

        var redactedFileName = fileName.toString();

        assertEquals("t***t.cy.js", redactedFileName);
    }
}
