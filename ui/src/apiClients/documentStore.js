import { useMemo } from "react";
import { setUrlHostToLocalHost } from "../utils/utils";
import { useApiRequest } from "./useApi";
import { useStorage } from "./useStorage";
import { documentUploadStates } from "../enums/documentUploads";

export const useDocumentStore = () => {
    const request = useApiRequest("doc-store-api");
    const storage = useStorage();

    const findByNhsNumber = async (nhsNumber) => {
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
    };

    const getPatientDetails = async (nhsNumber) => {
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
    };

    const getPresignedUrlForZip = async (nhsNumber) => {
        const { data } = await request.get("/DocumentManifest", {
            params: {
                "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
            },
        });
        return setUrlHostToLocalHost(data.result.url);
    };

    const deleteAllDocuments = async (nhsNumber) => {
        const { data } = await request.delete("/DocumentReference", {
            params: {
                "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
            },
        });
        return data.result.message;
    };

    const uploadDocument = async (document, nhsNumber, onUploadStateChange) => {
        const requestBody = {
            resourceType: "DocumentReference",
            subject: {
                identifier: {
                    system: "https://fhir.nhs.uk/Id/nhs-number",
                    value: nhsNumber,
                },
            },
            type: {
                coding: [
                    {
                        system: "http://snomed.info/sct",
                        code: "22151000087106",
                    },
                ],
            },
            content: [
                {
                    attachment: {
                        contentType: document.type,
                    },
                },
            ],
            description: document.name,
            created: "2021-07-11T16:57:30+01:00",
        };

        onUploadStateChange(documentUploadStates.UPLOADING, 0);

        try {
            const { data } = await request.post("/DocumentReference", requestBody);

            const url = data.content[0].attachment.url;
            let s3Url = setUrlHostToLocalHost(url);

            await storage.put(s3Url, document, {
                headers: {
                    "Content-Type": document.type,
                },
                onUploadProgress: ({ total, loaded }) => {
                    onUploadStateChange(documentUploadStates.UPLOADING, (loaded / total) * 100);
                },
            });
            onUploadStateChange(documentUploadStates.SUCCEEDED, 100);
        } catch (e) {
            onUploadStateChange(documentUploadStates.FAILED, 0);
        }
    };

    return useMemo(
        () => ({
            findByNhsNumber,
            getPatientDetails,
            getPresignedUrlForZip,
            deleteAllDocuments,
            uploadDocument,
        }),
        [request, storage]
    );
};
