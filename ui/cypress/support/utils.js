function logAccessibilityViolations(violations) {
    const accessibilityViolationsTitle = `${violations.length} accessibility violation${
        violations.length === 1 ? '' : 's'
    } ${violations.length === 1 ? 'was' : 'were'} detected`;
    const accessibilityViolationsData = violations.map(
        ({id, impact, description, nodes}) => ({
            id,
            impact,
            description,
            nodes: nodes.length
        })
    );

    cy.task('log', accessibilityViolationsTitle);
    cy.task('table', accessibilityViolationsData);
}

export {logAccessibilityViolations};
