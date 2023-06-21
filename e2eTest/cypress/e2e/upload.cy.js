import { logAccessibilityViolations } from "../support/utils";

describe("Uploads docs and tests it looks OK", () => {
    before(() => {
        cy.exec("./e2e-teardown.sh $ENVIRONMENT");
    });

    it("searches for a patient using their NHS number and uploads documents", () => {
        const baseUrl = Cypress.config("baseUrl");
        const nhsNumber = Cypress.env("REACT_APP_ENV") === "local" ? "9000000009" : "9449305552";
        const username = Cypress.env("username");
        const password = Cypress.env("password");
        const oidcProvider = Cypress.env("oidc_provider");
        const uploadedFilePathNames = [
            "cypress/fixtures/test_patient_record.pdf",
            "cypress/fixtures/test_patient_record_two.pdf",
        ];

        cy.visit("/");
        cy.title().should("eq", "Inactive Patient Record Administration");
        cy.injectAxe();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, false);
        cy.findByRole("button", { name: "Start now" }).click();

        if (oidcProvider === "cis2devoidc") {
            cy.findByPlaceholderText("User Name").type(username);
            cy.findByPlaceholderText("Password").type(password);
            cy.findByRole("button", { name: "Continue" }).click();
        }

        if (oidcProvider === "COGNITO") {
            cy.get('input[name="username"]:visible').type(username);
            cy.get('input[name="password"]:visible').type(password);
            cy.get('input[name="signInSubmitButton"]:visible').click();
        }

        cy.url().should("eq", baseUrl + "/home");
        cy.injectAxe();
        cy.findByRole("radio", { name: /Upload/ }).check();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, false);
        cy.findByRole("button", { name: "Continue" }).click();

        cy.url().should("eq", baseUrl + "/upload/search-patient");
        cy.findByRole("textbox", { name: "Enter NHS number" }).type(nhsNumber);
        cy.findByRole("button", { name: "Search" }).click();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.url().should("eq", baseUrl + "/upload/search-patient/result");
        cy.findByRole("button", { name: "Next" }).click();

        cy.url().should("eq", baseUrl + "/upload/submit");
        cy.get("input[type=file]").selectFile(uploadedFilePathNames);
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, false);
        cy.findByRole("button", { name: "Upload" }).click();
        cy.findByRole("table", {
            name: "Successfully uploaded documents",
        }).within(() => cy.findAllByRole("row").should("have.length", 3));
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, false);
        cy.findByRole("button", { name: "Start Again" }).click();

        cy.url().should("eq", Cypress.config("baseUrl") + "/home");

        cy.findByRole("link", { name: "Log Out" }).click();
        cy.url().should("eq", baseUrl + "/");
    });
});
