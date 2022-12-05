const {defineConfig} = require('cypress');

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:3000',
        defaultCommandTimeout: 5000,
        includeShadowDom: true,
        setupNodeEvents(on, config) {
            // implement node event listeners here
        },
        chromeWebSecurity: false,
        watchForFileChanges: false,
        videoUploadOnPasses: false
    },
});
