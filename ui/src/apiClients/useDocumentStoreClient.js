import { useAuth } from "react-oidc-context";
import axios from "axios";
import { useMemo } from "react";
import { useDocumentStoreAuthErrorInterceptor } from "./useDocumentStoreAuthErrorInterceptor";
import { useBaseAPIUrl } from "../providers/ConfigurationProvider";

const DOCUMENT_STORE_API = "doc-store-api";

export const useDocumentStoreClient = () => {
    const { user } = useAuth();
    const baseUrl = useBaseAPIUrl(DOCUMENT_STORE_API);
    const documentStoreAuthErrorInterceptor = useDocumentStoreAuthErrorInterceptor();

    return useMemo(() => {
        const axiosInstance = axios.create({
            baseURL: baseUrl,
            headers: {
                Accept: "application/fhir+json",
                Authorization: `Bearer ${user.id_token}`,
            },
        });
        axiosInstance.interceptors.response.use((response) => response, documentStoreAuthErrorInterceptor);

        return axiosInstance;
    }, [baseUrl, documentStoreAuthErrorInterceptor, user.id_token]);
};
