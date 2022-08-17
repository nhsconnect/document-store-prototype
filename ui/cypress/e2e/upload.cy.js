describe("upload transaction", () => {
  it("allows the user to upload a document", () => {
    cy.visit("/");
    // log into the website
    cy.login(Cypress.env("cognito_username"), Cypress.env("cognito_password"));
    // navigate to the upload document page
    cy.get('a[href="/upload"]').click();
    cy.url().should("eq", Cypress.config("baseUrl") + "/upload");
    // fill out fields on upload document page
    cy.get('input[name="nhsNumber"]').type("1234567890");
    cy.get('input[name="documentTitle"]').type("Jane Doe - Patient Record");
    cy.get('input[name="document"]').selectFile(
      "cypress/fixtures/test_patient_record.pdf"
    );
    cy.get('button[type="submit"]').click();
    // wait for lambda to return a success message
    cy.get('p[data-testid="success-message"]', { timeout: 20000 }).should(
      "be.visible"
    );
  });
});
