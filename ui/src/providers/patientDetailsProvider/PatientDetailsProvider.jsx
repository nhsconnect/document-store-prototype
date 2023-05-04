import { createContext, useContext, useState } from "react";

const PatientDetailsContext = createContext(null);

const PatientDetailsProvider = ({ children, value = null }) => {
    const patientDetails = useState(value);

    return <PatientDetailsContext.Provider value={patientDetails}>{children}</PatientDetailsContext.Provider>;
};

export default PatientDetailsProvider;
export const usePatientDetailsContext = () => useContext(PatientDetailsContext);
