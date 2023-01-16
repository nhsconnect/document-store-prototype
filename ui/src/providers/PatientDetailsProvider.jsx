import { createContext, useContext, useState } from "react";

const PatientDetailsProviderContext = createContext();

export const PatientDetailsProvider = ({ children }) => {
    const patientDetailsState = useState();
    return (
        <PatientDetailsProviderContext.Provider value={patientDetailsState}>
            {children}
        </PatientDetailsProviderContext.Provider>
    );
};

export const usePatientDetailsProviderContext = () => useContext(PatientDetailsProviderContext);
