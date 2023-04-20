import axios from "axios";
import { useMemo } from "react";
import { useBaseAPIUrl, useFeatureToggle } from "../providers/configProvider/ConfigProvider";

const DOCUMENT_STORE_API = "doc-store-api";

export const useDocumentStoreClient = (bearerToken, documentStoreAuthErrorInterceptor) => {
    const baseUrl = useBaseAPIUrl(DOCUMENT_STORE_API);
    const isCognitoActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return useMemo(() => {
        const headers = isCognitoActive
            ? {
                  Accept: "application/json",
                  Authorization: `Bearer ${bearerToken}`,
              }
            : {
                  Accept: "application/json",
              };
        const documentStoreReq = {
            baseURL: baseUrl,
            headers,
            withCredentials: !isCognitoActive,
        };

        const axiosInstance = axios.create(documentStoreReq);
        if (documentStoreAuthErrorInterceptor) {
            axiosInstance.interceptors.response.use((response) => response, documentStoreAuthErrorInterceptor);
        }
        return axiosInstance;
    }, [documentStoreAuthErrorInterceptor, baseUrl, isCognitoActive, bearerToken]);
};
