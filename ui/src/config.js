const config = {
    Auth: {
        region: '%region%',
        userPoolId: '%pool-id%',
        userPoolWebClientId: '%client-id%',
    },
    API: {
        endpoints: [
            {
                name: 'doc-store-api',
                endpoint: '%api-endpoint%'
            },
        ]
    }
    features: {
    },
};

export default config;
