import { useContext, createContext } from "react";
import { useAuth } from "react-oidc-context";
import { useDocumentStore } from "../../apiClients/documentStore";
import { useDocumentStoreAuthErrorInterceptor } from "../../apiClients/useDocumentStoreAuthErrorInterceptor";
import { useFeatureToggle } from "../configProvider/ConfigProvider";

const DocumentStoreContext = createContext(undefined);

const DocumentStoreProvider = ({ children }) => {
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return isOIDCAuthActive ? (
        <OIDCAuthorisedDocumentStoreProvider>{children}</OIDCAuthorisedDocumentStoreProvider>
    ) : (
        <SessionAuthorisedDocumentStoreProvider>{children}</SessionAuthorisedDocumentStoreProvider>
    );
};

const OIDCAuthorisedDocumentStoreProvider = ({ children }) => {
    const { user } = useAuth();
    const errorInterceptor = useDocumentStoreAuthErrorInterceptor();
    const documentStore = useDocumentStore(user.id_token, errorInterceptor);

    return <DocumentStoreContext.Provider value={documentStore}>{children}</DocumentStoreContext.Provider>;
};

const SessionAuthorisedDocumentStoreProvider = ({ children }) => {
    const documentStore = useDocumentStore();

    return <DocumentStoreContext.Provider value={documentStore}>{children}</DocumentStoreContext.Provider>;
};

export const useAuthorisedDocumentStore = () => useContext(DocumentStoreContext);

export default DocumentStoreProvider;
