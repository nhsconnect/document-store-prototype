import config from "../../src/config";

describe("upload and search transaction", () => {
    it("allows the user to upload a document and search for a document", () => {
        const nhsNumber = "9000000009";
        const documentTitle = "Jane Doe - Patient Record";
        cy.visit("/");

        if (
            config.features[Cypress.env("REACT_APP_ENV")]
                .CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED
        ) {
            cy.contains("Start now").click();
            // log into the website
            cy.cis2Login(Cypress.env("username"), Cypress.env("password"));
        } else {
            // log into the website
            cy.login(Cypress.env("username"), Cypress.env("password"));
        }

        cy.get("#upload", { timeout: 10000 }).click();
        cy.get('[type="submit"]').click();

        cy.url().should(
            "eq",
            Cypress.config("baseUrl") + "/upload/patient-trace"
        );
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.contains("Search").click();
        cy.contains("Next", { timeout: 30000 }).click();
        cy.url().should("eq", Cypress.config("baseUrl") + "/upload/submit");

        // fill out fields on upload document page
        cy.get('input[name="documentTitle"]').type(documentTitle);
        cy.get('input[name="document"]').selectFile(
            "cypress/fixtures/test_patient_record.pdf"
        );
        cy.get('button[type="submit"]').click();

        // upload success page
        cy.url({ timeout: 30000 }).should(
            "eq",
            Cypress.config("baseUrl") + "/upload/success"
        );
        cy.contains("Done").click();
        if (
            config.features[Cypress.env("REACT_APP_ENV")]
                .CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED
        ) {
            cy.url().should("eq", Cypress.config("baseUrl") + "/home");

            // return to the home page
            cy.visit("/");

            cy.contains("Start now").click();
        } else {
            cy.url().should("eq", Cypress.config("baseUrl") + "/");
        }

        // navigate to the view document page
        cy.get("#download").click();
        cy.get('[type="submit"]').click();

        cy.url().should(
            "eq",
            Cypress.config("baseUrl") + "/search/patient-trace"
        );
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.contains("Search").click();
        cy.contains("Next", { timeout: 30000 }).click();
        cy.url().should("eq", Cypress.config("baseUrl") + "/search/results");

        //mock window.open() to check if the link opens in new page
        cy.window().then((win) => {
            cy.stub(win, 'open', () => null).as("popup")
        });

        // wait for lambda to return results
        cy.get('a[data-testid="document-title"]', { timeout: 30000 })
            .first()
            .should("have.text", documentTitle)
            .click();

        cy.get('@popup', { timeout: 15000 }).should("be.called");

        cy.contains("Log Out").click();

        cy.url().should(
            "eq",
            Cypress.config("baseUrl") + "/"
        );
    });
});
