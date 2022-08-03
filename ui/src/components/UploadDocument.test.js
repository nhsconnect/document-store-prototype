import UploadDocument from "./UploadDocument";
import {render, screen} from '@testing-library/react';
import ApiClient from "../apiClients/apiClient";
import userEvent from "@testing-library/user-event";

jest.mock('../apiClients/apiClient')

test('renders page', () => {
    render(<UploadDocument />)
    screen.getByLabelText('Choose document')
    screen.getByText('Upload')
})

test('a document is uploaded when the upload button is clicked', () => {
    const apiClientMock = new ApiClient()
    const document = new File(['hello'], 'hello.txt', {type: 'text/plain'})
    render(<UploadDocument apiClient={apiClientMock} />)
    userEvent.upload(screen.getByLabelText('Choose document'), document)
    userEvent.click(screen.getByText('Upload'))
    expect(apiClientMock.uploadDocument).toHaveBeenCalledWith(document)
})