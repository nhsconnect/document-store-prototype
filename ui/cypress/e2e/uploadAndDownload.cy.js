import config from '../../src/config';
import * as path from 'path';

describe('upload and download', () => {
    it('searches for a patient, uploads, and then downloads their docs', () => {
        const nhsNumber = Math.floor(1000000000 + Math.random() * 9000000000).toString();
        const isCIS2Enabled = config.features[Cypress.env('REACT_APP_ENV')]
            .CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED;
        const username = Cypress.env('username');
        const password = Cypress.env('password');
        const uploadedFilePathNames = ['cypress/fixtures/test_patient_record.pdf', 'cypress/fixtures/test_patient_record_two.pdf'];
        const downloadedDocumentPath = path.join(Cypress.config('downloadsFolder'), `patient-record-${nhsNumber}.zip`);

        cy.visit('/');
        cy.title().should('eq', 'Document Store');

        if (isCIS2Enabled) {
            cy.contains('Start now').click();
            cy.cis2Login(username, password);
        } else {
            cy.login(username, password);
        }

        cy.get('#upload').click();
        cy.get('[type="submit"]').click();

        cy.url().should('eq', Cypress.config('baseUrl') + '/upload/patient-trace');
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.contains('Search').click();
        cy.contains('Next').click();

        cy.url().should('eq', Cypress.config('baseUrl') + '/upload/submit');
        cy.get('input[name="documents"]').selectFile(uploadedFilePathNames);
        cy.get('button[type="submit"]').click();

        cy.get('progress').should('have.length', 2);
        cy.contains('Successfully uploaded documents')
        cy.contains('Finish').click();

        if (isCIS2Enabled) {
            cy.url().should('eq', Cypress.config('baseUrl') + '/home');

            cy.visit('/');
            cy.contains('Start now').click();
        } else {
            cy.url().should('eq', Cypress.config('baseUrl') + '/');
        }

        cy.get('#download').click();
        cy.get('[type="submit"]').click();

        cy.url().should('eq', Cypress.config('baseUrl') + '/search/patient-trace');
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.contains('Search').click();
        cy.contains('Next').click();

        cy.url().should('eq', Cypress.config('baseUrl') + '/search/results');
        cy.readFile(downloadedDocumentPath).should('not.exist');
        cy.contains('Download All').click();
        cy.readFile(downloadedDocumentPath).should('exist');
        cy.get('span[role="alert"]').should('not.exist');

        cy.contains('Log Out').click();

        cy.url().should('eq', Cypress.config('baseUrl') + '/');
    });
});
