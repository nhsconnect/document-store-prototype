const {defineConfig} = require('cypress');

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:3000',
        defaultCommandTimeout: 30000,
        includeShadowDom: true,
        chromeWebSecurity: false,
        watchForFileChanges: false,
        videoUploadOnPasses: false
    },
});
