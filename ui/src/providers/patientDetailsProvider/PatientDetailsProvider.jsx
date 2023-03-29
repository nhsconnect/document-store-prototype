import { createContext, useContext, useState } from "react";

const PatientDetailsContext = createContext(undefined);

const PatientDetailsProvider = ({ children }) => {
    const patientDetails = useState();

    return <PatientDetailsContext.Provider value={patientDetails}>{children}</PatientDetailsContext.Provider>;
};

export default PatientDetailsProvider;
export const usePatientDetailsContext = () => useContext(PatientDetailsContext);
