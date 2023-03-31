import axios from "axios";
import { useMemo } from "react";
import { useBaseAPIUrl, useFeatureToggle } from "../providers/configProvider/ConfigProvider";

const DOCUMENT_STORE_API = "doc-store-api";

export const useDocumentStoreClient = (bearerToken, documentStoreAuthErrorInterceptor) => {
    const baseUrl = useBaseAPIUrl(DOCUMENT_STORE_API);
    const isOIDCAuthActive = useFeatureToggle("OIDC_AUTHENTICATION");

    return useMemo(() => {
        const headers = !isOIDCAuthActive
            ? {
                  Accept: "application/fhir+json",
              }
            : {
                  Accept: "application/fhir+json",
                  Authorization: `Bearer ${bearerToken}`,
              };

        const documentStoreReq = {
            baseURL: baseUrl,
            headers,
            withCredentials: !isOIDCAuthActive,
        };
        const axiosInstance = axios.create(documentStoreReq);
        if (documentStoreAuthErrorInterceptor) {
            axiosInstance.interceptors.response.use((response) => response, documentStoreAuthErrorInterceptor);
        }

        return axiosInstance;
    }, [documentStoreAuthErrorInterceptor, baseUrl, isOIDCAuthActive, bearerToken]);
};
