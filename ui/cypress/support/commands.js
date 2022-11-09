// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
Cypress.Commands.add("login", (username, password) => {
    cy.get("#username").type(username);
    cy.get("#password").type(password);
    cy.get('[data-test="sign-in-sign-in-button"]').last().click();
    cy.url().should("eq", Cypress.config("baseUrl") + "/");
});

Cypress.Commands.add("cis2Login", (username, password) => {
    cy.get('[placeholder="User Name"]').type(username);
    cy.get('[placeholder="Password"]').type(password);
    cy.contains("Continue").click();
});

Cypress.Commands.overwrite('visit', (originalFn, url, options) => {
    if (Cypress.env('basic_auth_username')) {
        originalFn(url, {
            ...options,
            auth: {
                username: Cypress.env('basic_auth_username'),
                password: Cypress.env('basic_auth_password')
            }
        })
    } else {
        originalFn(url, options)
    }
})
