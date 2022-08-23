import { createContext, useContext, useState } from "react";

const NhsNumberProviderContext = createContext();

export const NhsNumberProvider = ({ children }) => {
    const nhsNumberState = useState();
    return (
        <NhsNumberProviderContext.Provider value={nhsNumberState}>
            {children}
        </NhsNumberProviderContext.Provider>
    );
};

export const useNhsNumberProviderContext = () =>
    useContext(NhsNumberProviderContext);
