const config = {
    Auth: {
        region: "eu-west-2",
        userPoolId: "eu-west-2_Y8etyk9V6",
        userPoolWebClientId: "7mm2u6re8jsptrgallf74g2c6c",
        providerId: "COGNITO",
        oauth: {
            domain: "doc-store-user-pool.auth.eu-west-2.amazoncognito.com",
            scope: ["openid"],
            redirectSignIn: "http://localhost:3000/cis2-auth-callback",
            redirectSignOut: "http://localhost:3000",
            responseType: "token",
        },
    },
    API: {
        endpoints: [
            {
                name: "doc-store-api",
                endpoint: "http://localhost:3000/restapis/1d3tbej036/test/_user_request_",
            },
        ],
    },
    features: {
        local: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
        },
        dev: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true,
        },
        "pre-prod": {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
        },
        prod: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false
        }
    },
};

export default config;