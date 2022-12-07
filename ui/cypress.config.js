const {defineConfig} = require('cypress');

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:3000',
        defaultCommandTimeout: 30000,
        includeShadowDom: true,
        chromeWebSecurity: false,
        watchForFileChanges: false,
        videoUploadOnPasses: false,
        setupNodeEvents(on) {
            on('task', {
                log(message) {
                    console.log(message);
                    return null;
                },
                table(message) {
                    console.table(message);
                    return null;
                }
            })
        }
    },
});
