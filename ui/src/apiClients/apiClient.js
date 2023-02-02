import { setUrlHostToLocalHost } from "../utils/utils";
import axios from "axios";
import { documentUploadStates } from "../enums/documentUploads";

class ApiClient {
    constructor(api, user) {
        this.api = api;
        this.user = user;
    }

    async findByNhsNumber(nhsNumber) {
        const data = await this.api.get("doc-store-api", "/DocumentReference", {
            headers: {
                Accept: "application/fhir+json",
                Authorization: `Bearer ${this.user.id_token}`,
            },
            queryStringParameters: {
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
    }

    async uploadDocument(document, nhsNumber, onUploadStateChange) {
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

        const requestHeaders = {
            Accept: "application/fhir+json",
            Authorization: `Bearer ${this.user.id_token}`,
        };

        try {
            const response = await this.api.post("doc-store-api", "/DocumentReference", {
                body: requestBody,
                headers: requestHeaders,
            });

            const url = response.content[0].attachment.url;
            let s3Url = setUrlHostToLocalHost(url);

            await axios.put(s3Url, document, {
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
    }

    async getPatientDetails(nhsNumber) {
        return await this.api.get("doc-store-api", "/PatientDetails", {
            headers: {
                Accept: "application/json",
                Authorization: `Bearer ${this.user.id_token}`,
            },
            queryStringParameters: {
                "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
            },
        });
    }

    async getPresignedUrlForZip(nhsNumber) {
        const data = await this.api.get("doc-store-api", "/DocumentManifest", {
            headers: {
                Accept: "application/fhir+json",
                Authorization: `Bearer ${this.user.id_token}`,
            },
            queryStringParameters: {
                "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
            },
        });

        return setUrlHostToLocalHost(data.result.url);
    }

    async deleteAllDocuments(nhsNumber) {
        const data = await this.api.del("doc-store-api", "/DocumentReference", {
            headers: {
                Accept: "application/fhir+json",
                Authorization: `Bearer ${this.user.id_token}`,
            },
            queryStringParameters: {
                "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
            },
        });

        return data.result.message;
    }
}

export default ApiClient;
