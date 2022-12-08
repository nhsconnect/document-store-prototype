import * as path from 'path';

describe('upload and download', () => {
    it('searches for a patient, uploads, and then downloads their docs', () => {
        const baseUrl = Cypress.config('baseUrl');
        const nhsNumber = Math.floor(1000000000 + Math.random() * 9000000000).toString();
        const username = Cypress.env('username');
        const password = Cypress.env('password');
        const uploadedFilePathNames = ['cypress/fixtures/test_patient_record.pdf', 'cypress/fixtures/test_patient_record_two.pdf'];
        const downloadedDocumentPath = path.join(Cypress.config('downloadsFolder'), `patient-record-${nhsNumber}.zip`);

        cy.visit('/');
        cy.title().should('eq', 'Document Store');

        cy.contains('Start now').click();
        cy.cis2Login(username, password);

        cy.url().should('eq', baseUrl + '/home');

        cy.get('#upload').check();
        cy.get('form').submit();

        cy.url().should('eq', baseUrl + '/upload/patient-trace');
        cy.get('input').type(nhsNumber);
        cy.get('form').submit();
        cy.contains('Next').click();

        cy.url().should('eq', baseUrl + '/upload/submit');
        cy.get('input[type=file]').selectFile(uploadedFilePathNames);
        cy.get('form').submit();
        cy.get('table').contains('Your documents are uploading');
        cy.contains('Finish').click();

        cy.url().should('eq', Cypress.config('baseUrl') + '/home');

        cy.visit('/');
        cy.contains('Start now').click();

        cy.get('#download').then(($el) => {
            assert.isTrue(Cypress.dom.isAttached($el))
        })

        cy.get('#download').check();
        cy.get('form').submit();

        cy.url().should('eq', baseUrl + '/search/patient-trace');
        cy.get('input').type(nhsNumber);
        cy.get('form').submit();
        cy.contains('Next').click();

        cy.url().should('eq', baseUrl + '/search/results');
        cy.readFile(downloadedDocumentPath).should('not.exist');
        cy.contains('Download All').click();
        cy.readFile(downloadedDocumentPath).should('exist');
        cy.contains('Log Out').click();

        cy.url().should('eq', baseUrl + '/');
    });
});
