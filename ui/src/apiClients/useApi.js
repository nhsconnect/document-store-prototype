import ApiClient from "./apiClient";
import { API } from "aws-amplify";
import { useAuth } from "react-oidc-context";
import axios from "axios";
import config from "../config";
import { useMemo } from "react";

const useApi = () => {
    const { user } = useAuth();
    return new ApiClient(API, user);
};

export const useApiRequest = (apiName) => {
    const { user } = useAuth();
    const baseUrl = config.API.endpoints.find((endpoint) => endpoint.name === apiName).endpoint;
    return useMemo(
        () =>
            axios.create({
                baseURL: baseUrl,
                headers: {
                    Accept: "application/fhir+json",
                    Authorization: `Bearer ${user.id_token}`,
                },
            }),
        [baseUrl, user.id_token]
    );
};

export default useApi;
