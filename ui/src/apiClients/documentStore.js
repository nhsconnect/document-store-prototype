import { useMemo } from "react";
import { useApiRequest } from "./useApi";

export const useDocumentStore = () => {
    const request = useApiRequest("doc-store-api");

    return useMemo(
        () => ({
            findByNhsNumber: async (nhsNumber) => {
                const { data } = await request.get("/DocumentReference", {
                    params: {
                        "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
                    },
                });
                return data.total > 0
                    ? data.entry.map(({ resource }) => ({
                          id: resource.id,
                          description: resource.description,
                          indexed: new Date(resource.indexed),
                      }))
                    : [];
            },
            getPatientDetails: async (nhsNumber) => {
                const { data } = await request.get("/PatientDetails", {
                    headers: {
                        ...request.defaults.headers,
                        Accept: "application/json",
                    },
                    params: {
                        "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
                    },
                });
                return data;
            },
        }),
        [request]
    );
};
