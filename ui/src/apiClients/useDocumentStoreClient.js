import axios from "axios";
import { useMemo } from "react";
import { useBaseAPIUrl } from "../providers/configProvider/ConfigProvider";

const DOCUMENT_STORE_API = "doc-store-api";

export const useDocumentStoreClient = (bearerToken, documentStoreAuthErrorInterceptor) => {
    const baseUrl = useBaseAPIUrl(DOCUMENT_STORE_API);

    return useMemo(() => {
        const headers = {
            Accept: "application/json",
        };
        const documentStoreReq = {
            baseURL: baseUrl,
            headers,
            withCredentials: true,
        };

        const axiosInstance = axios.create(documentStoreReq);
        if (documentStoreAuthErrorInterceptor) {
            axiosInstance.interceptors.response.use((response) => response, documentStoreAuthErrorInterceptor);
        }
        return axiosInstance;
    }, [documentStoreAuthErrorInterceptor, baseUrl, bearerToken]);
};
