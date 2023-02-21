const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {
    app.use(
        "/restapis/*",
        createProxyMiddleware({
            target: "http://localhost:4566",
            changeOrigin: true,
        })
    );
};
