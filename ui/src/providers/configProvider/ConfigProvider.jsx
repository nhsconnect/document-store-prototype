import { createContext, useContext } from "react";

const ConfigContext = createContext(null);

export const useFeatureToggle = (toggleName) => {
    const config = useContext(ConfigContext);

    const features = config?.features[process.env["REACT_APP_ENV"]];

    if (features === undefined || features[toggleName] === undefined) {
        return false;
    }

    return features[toggleName];
};

export const useBaseAPIUrl = (apiName) => {
    const config = useContext(ConfigContext);

    const apiEndpoints = config?.API.endpoints;

    if (apiEndpoints === undefined) {
        throw Error(`Endpoint for ${apiName} is not configured`);
    }

    const endpointConfiguration = apiEndpoints.find((endpoint) => endpoint.name === apiName);

    if (endpointConfiguration === undefined) {
        throw Error(`Endpoint for ${apiName} is not configured`);
    }

    return endpointConfiguration.endpoint;
};

const ConfigProvider = ({ children, config }) => {
    return <ConfigContext.Provider value={config}>{children}</ConfigContext.Provider>;
};

export default ConfigProvider;
