import {createContext, useContext, useState} from "react";

const DeleteDocumentsResponseProviderContext = createContext();

export const DeleteDocumentsResponseProvider = ({ children }) => {
    const deleteDocumentsResponseState = useState();
    return <DeleteDocumentsResponseProviderContext.Provider value={deleteDocumentsResponseState}>{children}</DeleteDocumentsResponseProviderContext.Provider>;
};

export const useDeleteDocumentsResponseProviderContext = () => useContext(DeleteDocumentsResponseProviderContext);