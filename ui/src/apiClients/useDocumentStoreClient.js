import axios from "axios";
import { useMemo } from "react";
import { useBaseAPIUrl, useFeatureToggle } from "../providers/configProvider/ConfigProvider";
import { useSessionContext } from "../providers/sessionProvider/SessionProvider";

const DOCUMENT_STORE_API = "doc-store-api";

export const useDocumentStoreClient = (bearerToken, documentStoreAuthErrorInterceptor) => {
    console.log("Doc store client used");
    const baseUrl = useBaseAPIUrl(DOCUMENT_STORE_API);
    const isCognitoActive = useFeatureToggle("OIDC_AUTHENTICATION");
    console.log("OIDC FEATURE:", isCognitoActive);

    const [session] = useSessionContext();
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

        console.log("SETTING HEADERS REQ:", documentStoreReq);
        const axiosInstance = axios.create(documentStoreReq);
        if (documentStoreAuthErrorInterceptor) {
            axiosInstance.interceptors.response.use((response) => response, documentStoreAuthErrorInterceptor);
        }
        return axiosInstance;
    }, [documentStoreAuthErrorInterceptor, baseUrl, isCognitoActive, bearerToken, session]);
};
