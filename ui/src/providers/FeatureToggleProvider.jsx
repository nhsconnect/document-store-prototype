import { createContext, useContext } from "react";
import config from "../config";

const FeatureToggleContext = createContext();

export const useFeatureToggle = (toggleName) => {
  const featureToggles = useContext(FeatureToggleContext);
  if (featureToggles?.[toggleName] === undefined) {
    return false;
  }
  return featureToggles[toggleName];
};

const FeatureToggleProvider = ({ children }) => {
  return (
    <FeatureToggleContext.Provider
      value={config.features[process.env.NODE_ENV]}
    >
      {children}
    </FeatureToggleContext.Provider>
  );
};

export default FeatureToggleProvider;
