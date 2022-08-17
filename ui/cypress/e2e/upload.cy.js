describe("upload transaction", () => {
  it("allows the user to log in", () => {
    cy.visit("/");
    cy.login(Cypress.env("cognito_username"), Cypress.env("cognito_password"));
  });
});
