import ApiClient from "./apiClient";
import {documentUploadStates} from "../enums/documentUploads";
import axios from "axios";

jest.mock("axios");

const token = "token";
const user = {
    id_token: token
};

describe("test the findByNhsNumber method", () => {
    test("returns a list of documents associated with an NHS number", async () => {
        const getMock = jest.fn(() => {
            return responseBody;
        });
        const api = { get: getMock };
        const apiClient = new ApiClient(api, user);
        const nhsNumber = 12345;
        const requestHeaders = {
            Accept: "application/fhir+json",
            Authorization: `Bearer ${token}`,
        };
        const queryStringParametersMock = {
            "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
        };
        const resourceObject = {
            id: "123",
            description: "description",
            docStatus: "final",
            indexed: "2022-08-10T10:34:41.515050Z",
        };
        const responseBody = {
            total: 1,
            entry: [{ resource: resourceObject }],
        };
        const expectedReturnedDocumentList = [
            {
                id: resourceObject.id,
                description: resourceObject.description,
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
    test("makes the request to create a DocumentReference and upload document, providing progress updates", async () => {
        const metadataResponseBody = {
            content: [
                {
                    attachment: {
                        contentType: "text/plain",
                        url: "pre-signed-url-mock",
                    },
                },
            ],
        };
        const postMock = jest.fn(async (baseUrl, path, { onUploadProgress }) => {
            return new Promise((resolve) => {
                setTimeout(() => {
                    onUploadProgress()
                }, 1)

                setTimeout(() => {
                    resolve(metadataResponseBody)
                }, 2)
            })
        });
        const api = { post: postMock };
        const apiClient = new ApiClient(api, user);
        const fileName = "hello.txt";
        const document = new File(["hello"], fileName, {
            type: "text/plain",
        });
        const nhsNumber = "0987654321";

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
                        contentType: "text/plain",
                    },
                },
            ],
            description: document.name,
            created: "2021-07-11T16:57:30+01:00",
        };


        axios.put = jest.fn((async (s3url, document, { onUploadProgress }) => {
            return new Promise((resolve) => {
                setTimeout(() => {
                    onUploadProgress({ total: 10, loaded: 5 })
                }, 1)

                setTimeout(() => {
                    resolve()
                }, 2)
            })
        }))

        const onUploadStateChangeMock = jest.fn()
        await apiClient.uploadDocument(
            document,
            nhsNumber,
            onUploadStateChangeMock,
        );

        expect(postMock).toHaveBeenCalledWith(
            "doc-store-api",
            "/DocumentReference",
            expect.objectContaining({
                headers: {
                    Accept: "application/fhir+json",
                    Authorization: `Bearer ${token}`,
                },
                body: requestBody
            })
        );
        expect(axios.put).toHaveBeenCalledWith(
            metadataResponseBody.content[0].attachment.url,
            document,
            expect.anything()
        );
        expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.WAITING, 0)
        expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.STORING_METADATA, 0)
        expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.UPLOADING, 50)
        expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.SUCCEEDED, 100)
    });

    test("reports the upload as failed if the store metadata request fails", async () => {
        const postMock = jest.fn(() => {
            throw new Error("Request failed")
        });

        const api = { post: postMock };
        const apiClient = new ApiClient(api, user);
        const fileName = "hello.txt";
        const document = new File(["hello"], fileName, {
            type: "text/plain",
        });
        const nhsNumber = "0987654321";

        const onUploadStateChangeMock = jest.fn()
        await apiClient.uploadDocument(
            document,
            nhsNumber,
            onUploadStateChangeMock,
        );

        expect(postMock).toHaveBeenCalled()

        expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.FAILED, 0)
    })

    test("reports the upload as failed if the S3 upload request fails", async () => {
        const metadataResponseBody = {
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
            return metadataResponseBody
        });

        const api = { post: postMock };
        const apiClient = new ApiClient(api, user);
        const fileName = "hello.txt";
        const document = new File(["hello"], fileName, {
            type: "text/plain",
        });
        const nhsNumber = "0987654321";

        axios.put = jest.fn((async () => {
            throw new Error("S3 upload failed")
        }))

        const onUploadStateChangeMock = jest.fn()
        await apiClient.uploadDocument(
            document,
            nhsNumber,
            onUploadStateChangeMock,
        );

        expect(postMock).toHaveBeenCalled()

        expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.FAILED, 0)
    })
});

describe("tests the getPatientDetails method", () => {
    it("returns a patient trace when given the NHS number 9000000009", async () => {
        const nhsNumber = "9000000009";
        const patientObject = {
            "birthDate": "2010-10-22",
            "familyName": "Smith",
            "givenName": ["Jane"],
            "nhsNumber": nhsNumber,
            "postalCode": "LS1 6AE"
        };
        const getMock = jest.fn(() => {
            return responseBody;
        });
        const api = { get: getMock };
        const apiClient = new ApiClient(api, user);
        const requestHeaders = {
            Accept: "application/json",
            Authorization: `Bearer ${user.id_token}`,
        };
        const queryStringParametersMock = {
            "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
        };
        const responseBody = {
            result: { patientDetails: patientObject },
        };

        const returnedPatientBundle = await apiClient.getPatientDetails(
            nhsNumber
        );

        expect(getMock).toHaveBeenCalledWith(
            "doc-store-api",
            "/PatientDetails",
            expect.objectContaining({
                headers: requestHeaders,
                queryStringParameters: queryStringParametersMock,
            })
        );
        expect(returnedPatientBundle).toStrictEqual(responseBody);
    });

    it("returns error patient not found when given the NHS number 9111231130", async () => {
        const nhsNumber = "9111231130";
        const error = "Patient does not exist for given NHS number.";
        const getMock = jest.fn(() => {
            return responseBody;
        });
        const api = { get: getMock };
        const apiClient = new ApiClient(api, user);
        const requestHeaders = {
            Accept: "application/json",
            Authorization: `Bearer ${user.id_token}`,
        };
        const queryStringParametersMock = {
            "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
        };
        const responseBody = {
            error: error,
        };

        const returnedPatientBundle = await apiClient.getPatientDetails(
            nhsNumber
        );

        expect(getMock).toHaveBeenCalledWith(
            "doc-store-api",
            "/PatientDetails",
            expect.objectContaining({
                headers: requestHeaders,
                queryStringParameters: queryStringParametersMock,
            })
        );
        expect(returnedPatientBundle).toStrictEqual(responseBody);
    });
});

describe("test the getPresignedUrl method", () => {
    test("returns a presigned url associated with the document id ", async () => {
        const getMock = jest.fn(() => {
            return responseBody;
        });
        const api = { get: getMock };
        const apiClient = new ApiClient(api, user);
        const id = 12345;
        const requestHeaders = {
            Accept: "application/fhir+json",
            Authorization: `Bearer ${token}`,
        };
        const retrieveUrl = "retrieve-url";
        const responseBody = {
            docStatus: "final",
            content: [{ attachment: { url: retrieveUrl } }],
        };
        const returnedPresignedUrl = await apiClient.getPresignedUrl(id);

        expect(getMock).toHaveBeenCalledWith(
            "doc-store-api",
            "/DocumentReference/" + id,
            expect.objectContaining({
                headers: requestHeaders,
            })
        );
        expect(returnedPresignedUrl).toStrictEqual(responseBody.content[0].attachment);
    });
});

describe("test the getPresignedUrlForZip method", () => {
    test("returns a presigned url associated with zip of all documents related to an nhs number ", async () => {
        const getMock = jest.fn(() => {
            return expectedResponse;
        });
        const api = { get: getMock };
        const apiClient = new ApiClient(api, user);
        const nhsNumber = "1234567890";
        const requestHeaders = {
            Accept: "application/fhir+json",
            Authorization: `Bearer ${token}`,
        };
        const responseUrl = "presigned-url";
        const expectedResponse = {
            result:{
                url:responseUrl
            }
        };
        const queryStringParametersMock = {
            "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
        };

        const returnedPresignedUrl = await apiClient.getPresignedUrlForZip(nhsNumber);

        expect(getMock).toHaveBeenCalledWith(
            "doc-store-api",
            "/DocumentManifest",
            expect.objectContaining({
                headers: requestHeaders,
                queryStringParameters: queryStringParametersMock,
            })
        );
        expect(returnedPresignedUrl).toStrictEqual(responseUrl);
    });
});
