Cypress.Commands.overwrite('visit', (originalFn, url, options) => {
    if (Cypress.env('basic_auth_username')) {
        originalFn(url, {
            ...options,
            auth: {
                username: Cypress.env('basic_auth_username'),
                password: Cypress.env('basic_auth_password')
            }
        });
    } else {
        originalFn(url, options);
    }
});
