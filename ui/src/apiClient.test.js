import ApiClient from "./apiClient";
import { Auth, API } from "aws-amplify"
jest.mock('aws-amplify')

test('uploadDocument', () => {
    const apiClient = new ApiClient(Auth, API)
    apiClient.uploadDocument()
})