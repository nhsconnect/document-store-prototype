package uk.nhs.digital.docstore.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
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
    @Disabled("Library doesn't support chained file extensions")
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
    void returnFileNameWithoutExtensionWhenGettingBaseName() {
        var fileName = new FileName("hello.txt");

        assertEquals(fileName.getBaseName(), "hello");
    }

    @Test
    void returnFileExtensionWhenGettingExtension() {
        var fileName = new FileName("hello.txt");

        assertEquals(fileName.getExtension(), ".txt");
    }

    @Test
    void returnEmptyStringWhenThereIsNoExtension() {
        var fileName = new FileName("test-file");

        assertEquals(fileName.getExtension(), "");
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
