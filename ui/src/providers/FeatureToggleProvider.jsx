import { createContext, useContext } from "react";

const FeatureToggleContext = createContext();

export const useFeatureToggle = (toggleName) => {
    const featureToggles = useContext(FeatureToggleContext);
    if (featureToggles?.[toggleName] === undefined) {
        return false;
    }
    return featureToggles[toggleName];
};

const FeatureToggleProvider = ({ children, config }) => {
    return (
        <FeatureToggleContext.Provider
            value={config?.features[process.env.REACT_APP_ENV]}
        >
            {children}
        </FeatureToggleContext.Provider>
    );
};

export default FeatureToggleProvider;
