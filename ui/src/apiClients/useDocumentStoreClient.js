import axios from "axios";
import { useMemo } from "react";
import { useBaseAPIUrl } from "../providers/configProvider/ConfigProvider";

const DOCUMENT_STORE_API = "doc-store-api";

export const useDocumentStoreClient = (bearerToken, documentStoreAuthErrorInterceptor) => {
    const baseUrl = useBaseAPIUrl(DOCUMENT_STORE_API);

    return useMemo(() => {
        const axiosInstance = axios.create({
            baseURL: baseUrl,
            headers: {
                Accept: "application/fhir+json",
                Authorization: `Bearer ${bearerToken}`,
            },
        });
        if (documentStoreAuthErrorInterceptor) {
            axiosInstance.interceptors.response.use((response) => response, documentStoreAuthErrorInterceptor);
        }

        return axiosInstance;
    }, [baseUrl, documentStoreAuthErrorInterceptor, bearerToken]);
};
