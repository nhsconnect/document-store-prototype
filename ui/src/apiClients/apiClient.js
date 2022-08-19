import storageClient from "./storageClient";
import { setUrlHostToLocalHost } from "../utils/utils";

class ApiClient {
    constructor(api, auth) {
        this.api = api;
        this.auth = auth;
    }

    async findByNhsNumber(nhsNumber) {
        const data = await this.api.get("doc-store-api", "/DocumentReference", {
            headers: {
                Accept: "application/fhir+json",
                Authorization: `Bearer ${(await this.auth.currentSession())
                    .getIdToken()
                    .getJwtToken()}`,
            },
            queryStringParameters: {
                "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
            },
        });

        return data.total > 0
            ? data.entry.map(({ resource }) => ({
                  description: resource.description,
                  type: resource.type.coding
                      .map((coding) => coding.code)
                      .join(", "),
                  url:
                      resource.docStatus === "final"
                          ? setUrlHostToLocalHost(
                                resource.content[0].attachment.url
                            )
                          : "",
                  indexed: new Date(resource.indexed),
              }))
            : [];
    }

    async uploadDocument(document, nhsNumber, documentTitle, snomedCode) {
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
                        code: snomedCode,
                    },
                ],
            },
            content: [
                {
                    attachment: {
                        contentType: "text/plain",
                    },
                },
            ],
            description: documentTitle,
            created: "2021-07-11T16:57:30+01:00",
        };
        const token = (await this.auth.currentSession())
            .getIdToken()
            .getJwtToken();
        const requestHeaders = {
            Accept: "application/fhir+json",
            Authorization: `Bearer ${token}`,
        };
        const response = await this.api.post(
            "doc-store-api",
            "/DocumentReference",
            { body: requestBody, headers: requestHeaders }
        );
        const url = response.content[0].attachment.url;
        let s3Url = setUrlHostToLocalHost(url);
        await storageClient(s3Url, document);
        console.log("document uploaded");
    }

    async getPatientDetails(nhsNumber) {
        if (nhsNumber === "9999999999") {
            throw Error("Error");
        }
        if (nhsNumber === "0000000000") {
            return [];
        }
        return [
            {
                name: "Joe Bloggs",
                dateOfBirth: "05/10/2001",
                postcode: "AB1 2CD",
            },
        ];
    }
}

export default ApiClient;
