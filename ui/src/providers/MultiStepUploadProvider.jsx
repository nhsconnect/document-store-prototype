import { createContext, useContext, useState } from "react";

const MultiStepUploadProviderContext = createContext();

export const MultiStepUploadProvider = ({children}) => {
    const nhsNumberState = useState()
    return (
        <MultiStepUploadProviderContext.Provider value={nhsNumberState}>
            {children}
        </MultiStepUploadProviderContext.Provider>
    )
}

export const useMultiStepUploadProviderContext = () => useContext(MultiStepUploadProviderContext)