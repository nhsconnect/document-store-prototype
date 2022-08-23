import config from "../../src/config";

describe("upload and search transaction", () => {
    it("allows the user to upload a document and search for a document", () => {
        const nhsNumber = "9000000009";
        const documentTitle = "Jane Doe - Patient Record";
        cy.visit("/");

        // log into the website
        cy.login(
            Cypress.env("cognito_username"),
            Cypress.env("cognito_password")
        );

        if (
            config.features[process.env.NODE_ENV].PDS_TRACE_FOR_UPLOAD_ENABLED
        ) {
            // navigate to the upload document page
            cy.get('a[href="/upload/patient-trace"]').click();
            cy.url().should(
                "eq",
                Cypress.config("baseUrl") + "/upload/patient-trace"
            );
            cy.get('input[name="nhsNumber"]').type(nhsNumber);
            cy.contains("Search").click();
            cy.contains("Next", { timeout: 30000 }).click();
            cy.url().should("eq", Cypress.config("baseUrl") + "/upload/submit");
        } else {
            // navigate to the upload document page
            cy.get('a[href="/upload"]').click();
            cy.url().should("eq", Cypress.config("baseUrl") + "/upload");
            cy.get('input[name="nhsNumber"]').type(nhsNumber);
        }

        // fill out fields on upload document page
        cy.get('input[name="documentTitle"]').type(documentTitle);
        cy.get('input[name="document"]').selectFile(
            "cypress/fixtures/test_patient_record.pdf"
        );
        cy.get('button[type="submit"]').click();

        // wait for lambda to return a success message
        cy.get('p[data-testid="success-message"]', { timeout: 30000 }).should(
            "be.visible"
        );

        // return to the home page
        cy.visit("/");

        // navigate to the view document page
        cy.get('a[href="/search"]').click();
        cy.url().should("eq", Cypress.config("baseUrl") + "/search");

        //search for document
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.get('button[type="submit"]').click();

        // wait for lambda to return results
        cy.get('a[data-testid="document-title"]', { timeout: 30000 })
            .first()
            .should("have.text", documentTitle);
    });
});
