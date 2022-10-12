
const config = {
    Auth: {
        region: "eu-west-2",
        userPoolId: "eu-west-2_Y8etyk9V6",
        userPoolWebClientId: "7mm2u6re8jsptrgallf74g2c6c",
        providerId: "COGNITO",
        oauth: {
            domain: "doc-store-user-pool.auth.eu-west-2.amazoncognito.com",
            scope: ["openid"],
            redirectSignIn: "https://main.d1p55zjnm05qd2.amplifyapp.com/cis2-auth-callback",
            redirectSignOut: "%cognito-redirect-signout%",
            responseType: "token",
        },
    },
    API: {
        endpoints: [
            {
                name: "doc-store-api",
                endpoint: "http://localhost:3000/restapis/v4yxehtasl/test/_user_request_/"
            },
        ],
    },
    features: {
        local: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
        },
        development: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
        },
        production: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true,
        },
    },
};

export default config;
