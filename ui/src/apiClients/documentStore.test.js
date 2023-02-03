import { documentUploadStates } from "../enums/documentUploads";
import { useApiRequest } from "./useApi";
import { useDocumentStore } from "./documentStore";
import { renderHook } from "@testing-library/react-hooks";

jest.mock("./useApi");

describe("The document store API client", () => {
    it("returns a list of documents associated with an NHS number", async () => {
        const nhsNumber = 12345;
        const queryStringParameters = {
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

        const get = jest.fn(async (path, options) => {
            expect(path).toEqual("/DocumentReference");
            expect(options.params["subject.identifier"]).toEqual(queryStringParameters["subject.identifier"]);
            return { data: responseBody };
        });
        useApiRequest.mockImplementation(() => ({ get }));
        const { result } = renderHook(() => useDocumentStore());

        const returnedDocumentList = await result.current.findByNhsNumber(nhsNumber);

        expect(get).toHaveBeenCalled();
        expect(returnedDocumentList).toStrictEqual(expectedReturnedDocumentList);
    });

    // describe("uploadDocument()", () => {
    //     test("makes the request to create a DocumentReference and upload document, providing progress updates", async () => {
    //         const metadataResponseBody = {
    //             content: [
    //                 {
    //                     attachment: {
    //                         contentType: "text/plain",
    //                         url: "pre-signed-url-mock",
    //                     },
    //                 },
    //             ],
    //         };
    //         const postMock = jest.fn(async () => {
    //             return new Promise((resolve) => {
    //                 setTimeout(() => {
    //                     resolve(metadataResponseBody);
    //                 }, 2);
    //             });
    //         });
    //         const api = { post: postMock };
    //         const apiClient = new ApiClient(api, user);
    //         const fileName = "hello.txt";
    //         const document = new File(["hello"], fileName, {
    //             type: "text/plain",
    //         });
    //         const nhsNumber = "0987654321";

    //         const requestBody = {
    //             resourceType: "DocumentReference",
    //             subject: {
    //                 identifier: {
    //                     system: "https://fhir.nhs.uk/Id/nhs-number",
    //                     value: nhsNumber,
    //                 },
    //             },
    //             type: {
    //                 coding: [
    //                     {
    //                         system: "http://snomed.info/sct",
    //                         code: "22151000087106",
    //                     },
    //                 ],
    //             },
    //             content: [
    //                 {
    //                     attachment: {
    //                         contentType: "text/plain",
    //                     },
    //                 },
    //             ],
    //             description: document.name,
    //             created: "2021-07-11T16:57:30+01:00",
    //         };

    //         axios.put = jest.fn(async (s3url, document, { onUploadProgress }) => {
    //             return new Promise((resolve) => {
    //                 setTimeout(() => {
    //                     onUploadProgress({ total: 10, loaded: 5 });
    //                 }, 1);

    //                 setTimeout(() => {
    //                     resolve();
    //                 }, 2);
    //             });
    //         });

    //         const onUploadStateChangeMock = jest.fn();
    //         await apiClient.uploadDocument(document, nhsNumber, onUploadStateChangeMock);

    //         expect(postMock).toHaveBeenCalledWith(
    //             "doc-store-api",
    //             "/DocumentReference",
    //             expect.objectContaining({
    //                 headers: {
    //                     Accept: "application/fhir+json",
    //                     Authorization: `Bearer ${token}`,
    //                 },
    //                 body: requestBody,
    //             })
    //         );
    //         expect(axios.put).toHaveBeenCalledWith(
    //             metadataResponseBody.content[0].attachment.url,
    //             document,
    //             expect.anything()
    //         );
    //         expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.UPLOADING, 0);
    //         expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.UPLOADING, 50);
    //         expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.SUCCEEDED, 100);
    //     });

    //     test("reports the upload as failed if the store metadata request fails", async () => {
    //         const postMock = jest.fn(() => {
    //             throw new Error("Request failed");
    //         });

    //         const api = { post: postMock };
    //         const apiClient = new ApiClient(api, user);
    //         const fileName = "hello.txt";
    //         const document = new File(["hello"], fileName, {
    //             type: "text/plain",
    //         });
    //         const nhsNumber = "0987654321";

    //         const onUploadStateChangeMock = jest.fn();
    //         await apiClient.uploadDocument(document, nhsNumber, onUploadStateChangeMock);

    //         expect(postMock).toHaveBeenCalled();

    //         expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.FAILED, 0);
    //     });

    //     test("reports the upload as failed if the S3 upload request fails", async () => {
    //         const metadataResponseBody = {
    //             content: [
    //                 {
    //                     attachment: {
    //                         contentType: "text/plain",
    //                         url: "pre-signed-url-mock",
    //                     },
    //                 },
    //             ],
    //         };
    //         const postMock = jest.fn(() => {
    //             return metadataResponseBody;
    //         });

    //         const api = { post: postMock };
    //         const apiClient = new ApiClient(api, user);
    //         const fileName = "hello.txt";
    //         const document = new File(["hello"], fileName, {
    //             type: "text/plain",
    //         });
    //         const nhsNumber = "0987654321";

    //         axios.put = jest.fn(async () => {
    //             throw new Error("S3 upload failed");
    //         });

    //         const onUploadStateChangeMock = jest.fn();
    //         await apiClient.uploadDocument(document, nhsNumber, onUploadStateChangeMock);

    //         expect(postMock).toHaveBeenCalled();

    //         expect(onUploadStateChangeMock).toHaveBeenCalledWith(documentUploadStates.FAILED, 0);
    //     });
    // });

    // describe("getPatientDetails()", () => {
    //     it("returns a patient trace when given the NHS number 9000000009", async () => {
    //         const nhsNumber = "9000000009";
    //         const patientObject = {
    //             birthDate: "2010-10-22",
    //             familyName: "Smith",
    //             givenName: ["Jane"],
    //             nhsNumber: nhsNumber,
    //             postalCode: "LS1 6AE",
    //         };
    //         const getMock = jest.fn(() => {
    //             return responseBody;
    //         });
    //         const api = { get: getMock };
    //         const apiClient = new ApiClient(api, user);
    //         const requestHeaders = {
    //             Accept: "application/json",
    //             Authorization: `Bearer ${user.id_token}`,
    //         };
    //         const queryStringParametersMock = {
    //             "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
    //         };
    //         const responseBody = {
    //             result: { patientDetails: patientObject },
    //         };

    //         const returnedPatientBundle = await apiClient.getPatientDetails(nhsNumber);

    //         expect(getMock).toHaveBeenCalledWith(
    //             "doc-store-api",
    //             "/PatientDetails",
    //             expect.objectContaining({
    //                 headers: requestHeaders,
    //                 queryStringParameters: queryStringParametersMock,
    //             })
    //         );
    //         expect(returnedPatientBundle).toStrictEqual(responseBody);
    //     });
    // });

    // describe("getPresignedUrlForZip()", () => {
    //     test("returns a presigned url associated with zip of all documents related to an nhs number ", async () => {
    //         const getMock = jest.fn(() => {
    //             return expectedResponse;
    //         });
    //         const api = { get: getMock };
    //         const apiClient = new ApiClient(api, user);
    //         const nhsNumber = "1234567890";
    //         const requestHeaders = {
    //             Accept: "application/fhir+json",
    //             Authorization: `Bearer ${token}`,
    //         };
    //         const responseUrl = "presigned-url";
    //         const expectedResponse = {
    //             result: {
    //                 url: responseUrl,
    //             },
    //         };
    //         const queryStringParametersMock = {
    //             "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
    //         };

    //         const returnedPresignedUrl = await apiClient.getPresignedUrlForZip(nhsNumber);

    //         expect(getMock).toHaveBeenCalledWith(
    //             "doc-store-api",
    //             "/DocumentManifest",
    //             expect.objectContaining({
    //                 headers: requestHeaders,
    //                 queryStringParameters: queryStringParametersMock,
    //             })
    //         );
    //         expect(returnedPresignedUrl).toStrictEqual(responseUrl);
    //     });
    // });

    // describe("deleteAllDocuments()", () => {
    //     it("returns a success message after deleting the files related to an nhs number ", async () => {
    //         const expectedResponse = {
    //             result: {
    //                 message: "successfully deleted",
    //             },
    //         };
    //         const getMock = jest.fn(() => {
    //             return expectedResponse;
    //         });
    //         const api = { del: getMock };
    //         const apiClient = new ApiClient(api, user);
    //         const nhsNumber = "1234567890";
    //         const requestHeaders = {
    //             Accept: "application/fhir+json",
    //             Authorization: `Bearer ${token}`,
    //         };
    //         const queryStringParametersMock = {
    //             "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
    //         };
    //         const returnedDeleteResult = await apiClient.deleteAllDocuments(nhsNumber);

    //         expect(getMock).toHaveBeenCalledWith(
    //             "doc-store-api",
    //             "/DocumentReference",
    //             expect.objectContaining({
    //                 headers: requestHeaders,
    //                 queryStringParameters: queryStringParametersMock,
    //             })
    //         );
    //         expect(returnedDeleteResult).toStrictEqual(expectedResponse.result.message);
    //     });
    // });
});
