import { createContext, useContext } from "react";

const ConfigurationContext = createContext(undefined);

export const useFeatureToggle = (toggleName) => {
    const config = useContext(ConfigurationContext);

    const features = config?.features[process.env["REACT_APP_ENV"]];

    if (features === undefined || features[toggleName] === undefined) {
        return false;
    }

    return features[toggleName];
};

export const useBaseAPIUrl = (apiName) => {
    const config = useContext(ConfigurationContext);

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

const ConfigurationProvider = ({ children, config }) => {
    return <ConfigurationContext.Provider value={config}>{children}</ConfigurationContext.Provider>;
};

export default ConfigurationProvider;
