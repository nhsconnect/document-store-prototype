package uk.nhs.digital.docstore.authoriser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecureCookieTest {

    @Test
    void hasCorrectFieldsToMakeCookieSecure() {
        var expected = "SameSite=Strict; Secure; HttpOnly";
        var cookie = new SecureCookie();
        var actual = cookie.toString();

        assertThat(actual).contains(expected);
    }

    @Test
    void IncludesCustomFields() {
        var secureFields = "SameSite=Strict; Secure; HttpOnly";
        var expectedFields = "Field=contents; ";
        var cookie = new SecureCookie();
        cookie.addField("Field", "contents");
        var actual = cookie.toString();

        assertThat(actual).contains(expectedFields);
        assertThat(actual).contains(secureFields);
    }
}
