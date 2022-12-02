import config from "../../src/config";

describe("upload and search transaction", () => {
    it("allows the user to upload a document and search for a document", () => {
        // set a timeout that allows lambda cold starts
        const timeout = 30000;
        const nhsNumber = "9000000009";
        cy.visit("/");
        cy.title().should('eq', 'Document Store');

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

        cy.get("#upload", { timeout }).click();
        cy.get('[type="submit"]').click();

        cy.url().should(
            "eq",
            Cypress.config("baseUrl") + "/upload/patient-trace"
        );
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.contains("Search").click();
        cy.contains("Next", { timeout }).click();
        cy.url().should("eq", Cypress.config("baseUrl") + "/upload/submit");

        // fill out fields on upload document page
        cy.get('input[name="documents"]').selectFile(
            ["cypress/fixtures/test_patient_record.pdf", "cypress/fixtures/test_patient_record_two.pdf"]
        );
        cy.get('button[type="submit"]').click();
        cy.get("progress").should("have.length", 2);
        cy.contains("Successfully uploaded documents", {timeout})

        cy.contains("Finish").click();
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
        cy.contains("Next", { timeout }).click();
        cy.url().should("eq", Cypress.config("baseUrl") + "/search/results");

        cy.contains("Download All", { timeout }).click();

        cy.get('span[role="alert"]').should('not.exist');

        cy.contains("Log Out").click();

        cy.url().should(
            "eq",
            Cypress.config("baseUrl") + "/"
        );
    });
});
