import { useContext, createContext } from "react";
import { useAuth } from "react-oidc-context";
import { useDocumentStore } from "../../apiClients/documentStore";
import { useDocumentStoreAuthErrorInterceptor } from "../../apiClients/useDocumentStoreAuthErrorInterceptor";

const DocumentStoreContext = createContext(null);

const DocumentStoreProvider = ({ children }) => {
    <OIDCAuthorisedDocumentStoreProvider>{children}</OIDCAuthorisedDocumentStoreProvider>;
};

const OIDCAuthorisedDocumentStoreProvider = ({ children }) => {
    const { user } = useAuth();
    const errorInterceptor = useDocumentStoreAuthErrorInterceptor();
    const documentStore = useDocumentStore(user.id_token, errorInterceptor);

    return <DocumentStoreContext.Provider value={documentStore}>{children}</DocumentStoreContext.Provider>;
};

export const useAuthorisedDocumentStore = () => useContext(DocumentStoreContext);

export default DocumentStoreProvider;
