import config from '../../src/config';
import * as path from 'path';

describe('upload and search transaction', () => {
    it('allows the user to upload a document and search for a document', () => {
        const timeout = 30000;
        const randomNHSNumber = Math.floor(1000000000 + Math.random() * 9000000000).toString();
        const nhsNumber = Cypress.env('REACT_APP_ENV') === 'local' ? randomNHSNumber : '9000000009'

        cy.visit('/');
        cy.title().should('eq', 'Document Store');

        if (
            config.features[Cypress.env('REACT_APP_ENV')]
                .CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED
        ) {
            cy.contains('Start now').click();
            cy.cis2Login(Cypress.env('username'), Cypress.env('password'));
        } else {
            cy.login(Cypress.env('username'), Cypress.env('password'));
        }

        cy.get('#upload', {timeout}).click();
        cy.get('[type="submit"]').click();

        cy.url().should(
            'eq',
            Cypress.config('baseUrl') + '/upload/patient-trace'
        );
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.contains('Search').click();
        cy.contains('Next', {timeout}).click();
        cy.url().should('eq', Cypress.config('baseUrl') + '/upload/submit');

        cy.get('input[name="documents"]').selectFile(
            ['cypress/fixtures/test_patient_record.pdf', 'cypress/fixtures/test_patient_record_two.pdf']
        );
        cy.get('button[type="submit"]').click();
        cy.get('progress').should('have.length', 2);
        cy.contains('Successfully uploaded documents', {timeout})

        cy.contains('Finish').click();
        if (
            config.features[Cypress.env('REACT_APP_ENV')]
                .CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED
        ) {
            cy.url().should('eq', Cypress.config('baseUrl') + '/home');

            cy.visit('/');

            cy.contains('Start now').click();
        } else {
            cy.url().should('eq', Cypress.config('baseUrl') + '/');
        }

        cy.get('#download').click();
        cy.get('[type="submit"]').click();

        cy.url().should(
            'eq',
            Cypress.config('baseUrl') + '/search/patient-trace'
        );
        cy.get('input[name="nhsNumber"]').type(nhsNumber);
        cy.contains('Search').click();
        cy.contains('Next', {timeout}).click();
        cy.url().should('eq', Cypress.config('baseUrl') + '/search/results');

        const downloadedDocumentPath = path.join(Cypress.config("downloadsFolder"), `patient-record-${nhsNumber}.zip`);
        cy.readFile(downloadedDocumentPath).should('not.exist');
        cy.contains('Download All', {timeout}).click();
        cy.readFile(downloadedDocumentPath).should('exist');

        cy.get('span[role="alert"]').should('not.exist');

        cy.contains('Log Out').click();

        cy.url().should(
            'eq',
            Cypress.config('baseUrl') + '/'
        );
    });
});
