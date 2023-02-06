import { useAuth } from "react-oidc-context";
import axios from "axios";
import config from "../config";
import { useMemo } from "react";
import { useDocumentStoreAuthErrorInterceptor } from "./useDocumentStoreAuthErrorInterceptor";

export const useApiRequest = (apiName) => {
    const { user } = useAuth();
    const documentStoreAuthErrorInterceptor = useDocumentStoreAuthErrorInterceptor();

    const baseUrl = config.API.endpoints.find((endpoint) => endpoint.name === apiName).endpoint;

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
