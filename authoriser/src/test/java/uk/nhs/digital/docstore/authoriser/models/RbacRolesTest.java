package uk.nhs.digital.docstore.authoriser.models;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RbacRolesTest {

    @Test
    void shouldReturnFalseWhenNoMatchingTertiaryRoleCodeIsFound() {
        var userRoles = List.of(new Role("Code1:Code4"));
        var validRoles = List.of("Code1");
        RbacRoles rbacRole = new RbacRoles(userRoles);
        assertFalse(rbacRole.containsAnyTertiaryRole(validRoles));
    }

    @Test
    void shouldReturnTrueWhenAMatchingTertiaryRoleCodeIsFound() {
        var userRoles = List.of(new Role("Code1:Code4"));
        var validRoles = List.of("Code4");
        RbacRoles rbacRole = new RbacRoles(userRoles);
        assertTrue(rbacRole.containsAnyTertiaryRole(validRoles));
    }

    @Test
    void shouldReturnTrueWhenTheLastValidRoleCodeMatchesAUserRole() {
        var userRoles = List.of(new Role("Code1:Code4"));
        var validRoles = List.of("Code2", "Code3", "Code4");
        RbacRoles rbacRole = new RbacRoles(userRoles);
        assertTrue(rbacRole.containsAnyTertiaryRole(validRoles));
    }

    @Test
    void shouldReturnTrueWhenTheUserHasMultipleRolesWithMultipleCodes() {
        var userRoles = List.of(new Role("Code1:Code4"), new Role("Code2:Code3:Code5:Code6"));
        var validRoles = List.of("Code6", "Code3");
        RbacRoles rbacRole = new RbacRoles(userRoles);
        assertTrue(rbacRole.containsAnyTertiaryRole(validRoles));
    }
}
