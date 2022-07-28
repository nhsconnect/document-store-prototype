import ApiClient from "./apiClient";
import uploadDocumentWithStorageClient from "./storageClient";
jest.mock("./storageClient")


describe('uploadDocument', () => {
    test('makes the request to create a DocumentReference and upload document', async () => {
        const responseBody = {
            "content": [
                {
                    "attachment": {
                        "contentType": "text/plain",
                        "url": "pre-signed-url-mock"
                    }
                }
            ],
        }
        const postMock = jest.fn(() => {return responseBody})
        const token = "token"
        const auth = {currentSession: async () => {return {getIdToken: () => {return {getJwtToken: () => {return token}}}}}}
        const api = {post: postMock}
        const apiClient = new ApiClient(api, auth)
        const document = new File(['hello'], 'hello.txt', {type: 'text/plain'})
        const requestBody = {
            "resourceType": "DocumentReference",
            "subject": {
                "identifier": {
                    "system": "https://fhir.nhs.uk/Id/nhs-number",
                    "value": "34567"
                }
            },
            "type": {
                "coding": [
                    {
                        "system": "http://snomed.info/sct",
                        "code": "962381000000101"
                    }
                ]
            },
            "content": [
                {
                    "attachment": {
                        "contentType": "text/plain"
                    }
                }
            ],
            "description": "new document",
            "created": "2021-07-11T16:57:30+01:00"
        }

        await apiClient.uploadDocument(document)
        expect(postMock).toHaveBeenCalledWith("doc-store-api", '/DocumentReference', expect.objectContaining({body: requestBody}))
        expect(uploadDocumentWithStorageClient).toHaveBeenCalledWith(responseBody.content[0].attachment.url, document, token)
    })
})
