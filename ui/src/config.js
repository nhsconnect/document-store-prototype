const config = {
    Auth: {
        region: "%region%",
        userPoolId: "%pool-id%",
        userPoolWebClientId: "%client-id%",
        oauth: {
            domain: "%cognito-domain%",
            scope: ["openid"],
            redirectSignIn: "https://main.%amplify-app-id%.amplifyapp.com/",
            redirectSignOut: "%cognito-redirect-signout%",
            responseType: "code"
        }
    },
    API: {
        endpoints: [
            {
                name: "doc-store-api",
                endpoint: "%api-endpoint%",
            },
        ],
    },
    features: {
        local: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true,
            PDS_TRACE_FOR_UPLOAD_ENABLED: false,
        },
        development: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true,
            PDS_TRACE_FOR_UPLOAD_ENABLED: false,
        },
        production: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true,
            PDS_TRACE_FOR_UPLOAD_ENABLED: false,
        },
    },
};

export default config;
