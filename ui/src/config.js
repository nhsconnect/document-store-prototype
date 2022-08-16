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
      SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE: true,
    },
    development: {
      CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
      SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE: true,
    },
    production: {
      CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
      SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE: false,
    },
  },
};

export default config;
