import * as path from 'path';
import {logAccessibilityViolations} from '../support/utils';

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
        cy.findByRole('button', {name: 'Start now'}).click();

        cy.url().should('eq', baseUrl + '/home');
        cy.findByPlaceholderText('User Name').type(username);
        cy.findByPlaceholderText('Password').type(password);
        cy.findByRole('button', {name: 'Continue'}).click();

        cy.url().should('eq', baseUrl + '/home');
        cy.injectAxe();
        // Todo: Replace cy.wait() with a guard to wait until React has completed it's initial render(s)
        cy.wait(500);
        cy.findByRole('radio', {name: /Upload/}).check()
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.findByRole('button', {name: 'Continue'}).click();

        cy.url().should('eq', baseUrl + '/upload/patient-trace');
        cy.findByRole('textbox', {name: 'NHS number'}).type(nhsNumber);
        cy.findByRole('button', {name: 'Search'}).click();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.findByRole('button', {name: 'Next'}).click();

        cy.url().should('eq', baseUrl + '/upload/submit');
        cy.get('input[type=file]').selectFile(uploadedFilePathNames);
        cy.findByRole('button', {name: 'Upload'}).click();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.findByRole('button', {name: 'Finish'}).click();

        cy.url().should('eq', Cypress.config('baseUrl') + '/home');
        cy.visit('/');
        cy.findByRole('button', {name: 'Start now'}).click();

        cy.url().should('eq', baseUrl + '/home');
        cy.injectAxe();
        // Todo: Replace cy.wait() with a guard to wait until React has completed it's initial render(s)
        cy.wait(500);
        cy.findByRole('radio', {name: /Download/}).check();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.findByRole('button', {name: 'Continue'}).click();

        cy.url().should('eq', baseUrl + '/search/patient-trace');
        cy.findByRole('textbox', {name: 'NHS number'}).type(nhsNumber);
        cy.findByRole('button', {name: 'Search'}).click();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.findByRole('button', {name: 'Next'}).click();

        cy.url().should('eq', baseUrl + '/search/results');
        cy.readFile(downloadedDocumentPath).should('not.exist');
        cy.findByRole('button', {name: 'Download All'}).click();
        cy.readFile(downloadedDocumentPath).should('exist');
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.findByRole('button', {name: 'Log Out'}).click();

        cy.url().should('eq', baseUrl + '/');
    });
});
