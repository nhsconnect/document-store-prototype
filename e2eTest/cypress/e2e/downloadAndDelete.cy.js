// import * as path from "path";
import { logAccessibilityViolations } from "../support/utils";

describe("downloads and deletes docs", () => {
    before(() => {
        cy.exec("./e2e-teardown.sh $ENVIRONMENT");
        cy.exec("./e2e-download-journey-setup.sh $ENVIRONMENT");
    });

    it("searches for a patient, downloads, and then deletes docs", () => {
        // const baseUrl = Cypress.config("baseUrl");
        // const nhsNumber = "9449305552";
        const username = Cypress.env("username");
        const password = Cypress.env("password");
        const oidcProvider = Cypress.env("oidc_provider");
        // const downloadedDocumentPath = path.join(Cypress.config("downloadsFolder"), `patient-record-${nhsNumber}.zip`);

        cy.visit("/");
        cy.title().should("eq", "Inactive Patient Record Administration");
        cy.injectAxe();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, false);
        cy.findByRole("button", { name: "Start now" }).click();

        if (oidcProvider === "cis2devoidc") {
            cy.findByText("User Name").type(username);
            cy.findByText("Password").type(password);
            cy.findByRole("button", { name: "Continue" }).click();
        }

        // Temporarily disabled until valid PCSE CIS2 user is provided
        // cy.url().should("eq", baseUrl + "/search/patient");
        // cy.findByRole("textbox", { name: "Enter NHS number" }).type(nhsNumber);
        // cy.findByRole("button", { name: "Search" }).click();
        // cy.url().should("eq", baseUrl + "/search/patient/result");
        // cy.findByRole("button", { name: "Accept details are correct" }).click();
        //
        // cy.url().should("eq", baseUrl + "/search/results");
        // cy.readFile(downloadedDocumentPath).should("not.exist");
        // cy.findByRole("button", { name: "Download All Documents" }).click();
        // cy.readFile(downloadedDocumentPath).should("exist");
        // cy.checkA11y(undefined, undefined, logAccessibilityViolations, false);
        //
        // cy.findByRole("button", { name: "Delete All Documents" }).click();
        // cy.url().should("eq", baseUrl + "/search/results/delete");
        // cy.findByRole("radio", { name: "Yes" }).check();
        // cy.checkA11y(undefined, undefined, logAccessibilityViolations, false);
        // cy.findByRole("button", { name: "Continue" }).click();
        // cy.url().should("eq", baseUrl + "/search/results");
        //
        // cy.findByRole("link", { name: "Log Out" }).click();
        // cy.url().should("eq", baseUrl + "/");
    });
});
