package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void throwsExceptionWhenFileNameIsEmptyString() {
        var fileName = new FileName("");

        assertThrows(StringIndexOutOfBoundsException.class, fileName::toString);
    }

    @Test
    void isEqualWhenFileNameValuesAreSame() {
        var fileName1 = new FileName("filename1.pdf");
        var fileName2 = new FileName("filename1.pdf");

        assertEquals(fileName1, fileName2);
    }

    @Test
    void isNotEqualWhenFileNameValuesAreDifferent() {
        var fileName1 = new FileName("filename1.pdf");
        var fileName2 = new FileName("filename2.js");

        assertNotEquals(fileName1, fileName2);
    }
}
