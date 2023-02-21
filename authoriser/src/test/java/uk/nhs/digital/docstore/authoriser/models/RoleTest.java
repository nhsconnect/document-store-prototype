package uk.nhs.digital.docstore.authoriser.models;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class RoleTest {

    @Test
    void shouldReturnFalseWhenNoMatchingTertiaryRoleCodeIsFound() {
        var userRole = new Role("Code1:Code4");
        var validRoles = List.of("Code1");
        assertFalse(userRole.containsAnyTertiaryRole(validRoles));
    }

    @Test
    void shouldReturnTrueWhenAMatchingTertiaryRoleCodeIsFound() {
        var userRole = new Role("Code1:Code4");
        var validRoles = List.of("Code4");
        assertTrue(userRole.containsAnyTertiaryRole(validRoles));
    }

    @Test
    void shouldReturnTrueWhenTheLastValidRoleCodeMatchesAUserRole() {
        var userRole = new Role("Code1:Code4");
        var validRoles = List.of("Code2", "Code3", "Code4");
        assertTrue(userRole.containsAnyTertiaryRole(validRoles));
    }
}
