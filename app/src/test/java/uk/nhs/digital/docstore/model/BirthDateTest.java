package uk.nhs.digital.docstore.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BirthDateTest {

    @Test
    void shouldRedactValidBirthDate() {
        assertThat(new BirthDate("2013-12-06").toString()).isEqualTo("****-12-06");
    }

    @Test
    void shouldRedactIncompleteBirthDate() {
        assertThat(new BirthDate("2013").toString()).isEqualTo("****");
    }

}