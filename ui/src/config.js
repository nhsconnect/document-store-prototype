const config = {
    Auth: {
        region: "eu-west-2",
        userPoolId: "eu-west-2_XDRU8Uou1",
        userPoolWebClientId: "kmj0d2q2j4muf4e4tp2t0954r",
    },
    API: {
        endpoints: [
            {
                name: "doc-store-api",
                endpoint:
                    "http://localhost:3000/restapis/c4wrs76myz/test/_user_request_/",
            },
        ],
    },
    features: {
        local: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: true,
            SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE: true,
        },
        development: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
            SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE: false,
        },
        production: {
            CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
            SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE: false,
        },
    },
};

export default config;
