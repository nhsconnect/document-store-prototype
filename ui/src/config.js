const config = {
    Auth: {
        region: "%region%",
        userPoolId: "%pool-id%",
        userPoolWebClientId: "%client-id%",
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
        },
        development: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
        },
        production: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
        },
    },
};

export default config;
