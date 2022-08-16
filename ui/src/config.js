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
      PDS_TRACE_FOR_UPLOAD_ENABLED: true,
    },
    development: {
      CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
      PDS_TRACE_FOR_UPLOAD_ENABLED: true,
    },
    production: {
      CIS2_FEDERATED_IDENTITY_PROVIDER_ENABLED: false,
      PDS_TRACE_FOR_UPLOAD_ENABLED: false,
    },
  },
};

export default config;
