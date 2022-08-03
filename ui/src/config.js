const config = {
    Auth: {
        region: 'eu-west-2',
        userPoolId: 'eu-west-2_XDRU8Uou1',
        userPoolWebClientId: 'kmj0d2q2j4muf4e4tp2t0954r',
    },
    API: {
        endpoints: [
            {
                name: 'doc-store-api',
                endpoint: 'http://localhost:4566/restapis/dp4cs4y2f4/test/_user_request_/'
            },
        ]
    }
};

export default config;
