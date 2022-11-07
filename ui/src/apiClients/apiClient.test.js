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
        const resourceObject = {
            id: "123",
            description: "description",
            docStatus: "final",
            indexed: "2022-08-10T10:34:41.515050Z",
            type: typeObject,
        };
        const responseBody = {
            total: 1,
            entry: [{ resource: resourceObject }],
        };
        const expectedReturnedDocumentList = [
            {
                id: resourceObject.id,
                description: resourceObject.description,
                type: snomedCode,
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

        await apiClient.uploadDocument(
            document,
            nhsNumber,
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

describe("tests the getPatientDetails method", () => {
    it("returns a patient trace when given the NHS number 9000000009", async () => {
        const patientObject = {
            id: "9000000009",
            name: [
                {
                    given: ["Joe"],
                    family: "Bloggs",
                },
            ],
            birthDate: "2001-10-05",
            address: [
                {
                    postalCode: "AB1 2CD",
                },
            ],
        };
        const getMock = jest.fn(() => {
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
        const api = { get: getMock };
        const apiClient = new ApiClient(api, auth);
        const nhsNumber = "9000000009";
        const requestHeaders = {
            Accept: "application/fhir+json",
            Authorization: `Bearer ${(await auth.currentSession())
                .getIdToken()
                .getJwtToken()}`,
        };
        const queryStringParametersMock = {
            "subject.identifier": `https://fhir.nhs.uk/Id/nhs-number|${nhsNumber}`,
        };
        const responseBody = {
            total: 1,
            entry: [{ resource: patientObject }],
        };
        const expectedPatientBundle = [
            {
                name: {
                    given: ["Joe"],
                    family: "Bloggs",
                },
                dateOfBirth: new Date(Date.UTC(2001, 9, 5)),
                postcode: "AB1 2CD",
            },
        ];

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
        expect(returnedPatientBundle).toStrictEqual(expectedPatientBundle);
    });
});

describe("test the getPresignedUrl method", () => {
  test("returns a presigned url associated with the document id ", async () => {
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
    const id = 12345;
    const requestHeaders = {
      Accept: "application/fhir+json",
      Authorization: `Bearer ${(await auth.currentSession())
        .getIdToken()
        .getJwtToken()}`,
    };
    const retrieveUrl = "retrieve-url";
    const responseBody = {
      docStatus: "final",
      content: [{ attachment: { url: retrieveUrl }}],
    };
    const returnedPresignedUrl = await apiClient.getPresignedUrl(id);

    expect(getMock).toHaveBeenCalledWith(
      "doc-store-api",
      "/DocumentReference/"+id,
      expect.objectContaining({
        headers: requestHeaders,
      })
    );
    expect(returnedPresignedUrl).toStrictEqual(responseBody.content[0].attachment);
  });
});
