import { useAuth } from "react-oidc-context";
import axios from "axios";
import { useConfig } from "../utils/useConfig";
import { useMemo } from "react";

export const useApiRequest = (apiName) => {
    const { user } = useAuth();
    const { API } = useConfig();
    const baseUrl = API.endpoints.find((endpoint) => endpoint.name === apiName).endpoint;
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
