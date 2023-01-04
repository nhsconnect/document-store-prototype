Cypress.Commands.overwrite("visit", (originalFn, url, options) => {
    if (Cypress.env("basic_auth_username")) {
        originalFn(url, {
            ...options,
            auth: {
                username: Cypress.env("basic_auth_username"),
                password: Cypress.env("basic_auth_password"),
            },
        });
    } else {
        originalFn(url, options);
    }
});

// This is necessary to use the Cognito hosted UI with a Cognito user pool OIDC provider
Cypress.Commands.add("disableSameSiteCookieRestrictions", () => {
    cy.intercept("*", (req) => {
        req.on("response", (res) => {
            if (!res.headers["set-cookie"]) {
                return;
            }

            const disableSameSite = (headerContent) => {
                return headerContent.replace(
                    /samesite=(lax|strict)/gi,
                    "samesite=none"
                );
            };

            if (Array.isArray(res.headers["set-cookie"])) {
                res.headers["set-cookie"] =
                    res.headers["set-cookie"].map(disableSameSite);
            } else {
                res.headers["set-cookie"] = disableSameSite(
                    res.headers["set-cookie"]
                );
            }
        });
    });
});
