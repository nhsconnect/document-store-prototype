import 'cypress-axe';
import * as path from 'path';
import config from '../../src/config';
import {logAccessibilityViolations} from '../support/utils';

describe('upload and download', () => {
    it('searches for a patient, uploads, and then downloads their docs', () => {
        const baseUrl = Cypress.config('baseUrl');
        const username = Cypress.env('username');
        const password = Cypress.env('password');
        const isCIS2Enabled = config.features[Cypress.env('REACT_APP_ENV')].CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED;
        const nhsNumber = Math.floor(1000000000 + Math.random() * 9000000000).toString();
        const uploadedFilePathNames = ['cypress/fixtures/test_patient_record.pdf', 'cypress/fixtures/test_patient_record_two.pdf'];
        const downloadedDocumentPath = path.join(Cypress.config('downloadsFolder'), `patient-record-${nhsNumber}.zip`);

        cy.visit('/');
        cy.title().should('eq', 'Document Store');

        if (isCIS2Enabled) {
            cy.contains('Start now').click();
            cy.cis2Login(username, password);

            cy.url().should('eq', baseUrl + '/home');
        } else {
            cy.login(username, password);

            cy.url().should('eq', baseUrl + '/');
        }

        cy.injectAxe();

        cy.get('#upload').check();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.get('form').submit();

        cy.url().should('eq', baseUrl + '/upload/patient-trace');
        cy.get('input').type(nhsNumber);
        cy.get('form').submit();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.contains('Next').click();

        cy.url().should('eq', baseUrl + '/upload/submit');
        cy.get('input[type=file]').selectFile(uploadedFilePathNames);
        cy.get('form').submit();
        cy.get('table').contains('Your documents are uploading');
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.contains('Finish').click();

        if (isCIS2Enabled) {
            cy.url().should('eq', baseUrl + '/home');

            cy.visit('/');
            cy.contains('Start now').click();
        } else {
            cy.url().should('eq', baseUrl + '/');
        }

        cy.get('#download').check();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.get('form').submit();

        cy.url().should('eq', baseUrl + '/search/patient-trace');
        cy.get('input').type(nhsNumber);
        cy.get('form').submit();
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.contains('Next').click();

        cy.url().should('eq', baseUrl + '/search/results');
        cy.readFile(downloadedDocumentPath).should('not.exist');
        cy.contains('Download All').click();
        cy.readFile(downloadedDocumentPath).should('exist');
        cy.checkA11y(undefined, undefined, logAccessibilityViolations, true);
        cy.contains('Log Out').click();

        cy.url().should('eq', baseUrl + '/');
    });
});
