import ApiClient from "./apiClient";
import uploadDocumentWithStorageClient from "./storageClient";
jest.mock("./storageClient");

describe("test the findByNhsNumber method", () => {
    test("returns a list of documents associated with an NHS number", async () => {
        const token = "token";
        const auth = {
            currentSession: async () => {
                return {
                    getIdToken: () => {
                        return {
                            getJwtToken: () => {
                                return token;
                            },
                        };
                    },
                };
            },
        };
        const getMock = jest.fn(() => {
            return responseBody;
        });
        const api = { get: getMock };
        const apiClient = new ApiClient(api, auth);
        const nhsNumber = 12345;
        const requestHeaders = {
            Accept: "application/fhir+json",
            Authorization: `Bearer ${(await auth.currentSession())
                .getIdToken()
                .getJwtToken()}`,
        };
        const queryStringParametersMock = {
            "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
        };
        const snomedCode = "185361000000102";
        const typeObject = { coding: [{ code: snomedCode }] };
        const retrieveUrl = "retrieve-url";
        const contentObject = [{ attachment: { url: retrieveUrl } }];
        const resourceObject = {
            description: "description",
            docStatus: "final",
            indexed: "2022-08-10T10:34:41.515050Z",
            type: typeObject,
            content: contentObject,
        };
        const responseBody = {
            total: 1,
            entry: [{ resource: resourceObject }],
        };
        const expectedReturnedDocumentList = [
            {
                description: resourceObject.description,
                type: snomedCode,
                url: retrieveUrl,
                indexed: new Date(Date.UTC(2022, 7, 10, 10, 34, 41, 515)),
            },
        ];

        const returnedDocumentList = await apiClient.findByNhsNumber(nhsNumber);

        expect(getMock).toHaveBeenCalledWith(
            "doc-store-api",
            "/DocumentReference",
            expect.objectContaining({
                headers: requestHeaders,
                queryStringParameters: queryStringParametersMock,
            })
        );
        expect(returnedDocumentList).toStrictEqual(
            expectedReturnedDocumentList
        );
    });
});

describe("test the uploadDocument method", () => {
    test("makes the request to create a DocumentReference and upload document", async () => {
        const responseBody = {
            content: [
                {
                    attachment: {
                        contentType: "text/plain",
                        url: "pre-signed-url-mock",
                    },
                },
            ],
        };
        const postMock = jest.fn(() => {
            return responseBody;
        });
        const token = "token";
        const auth = {
            currentSession: async () => {
                return {
                    getIdToken: () => {
                        return {
                            getJwtToken: () => {
                                return token;
                            },
                        };
                    },
                };
            },
        };
        const api = { post: postMock };
        const apiClient = new ApiClient(api, auth);
        const document = new File(["hello"], "hello.txt", {
            type: "text/plain",
        });
        const nhsNumber = "0987654321";
        const snomedCode = "22151000087106";
        const documentTitle = "Jane Doe - Patient Record";
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

        await apiClient.uploadDocument(
            document,
            nhsNumber,
            documentTitle,
            snomedCode
        );

        expect(postMock).toHaveBeenCalledWith(
            "doc-store-api",
            "/DocumentReference",
            expect.objectContaining({ body: requestBody })
        );
        expect(uploadDocumentWithStorageClient).toHaveBeenCalledWith(
            responseBody.content[0].attachment.url,
            document
        );
    });
});
