import {Button, Input} from "nhsuk-react-components"
import React, {useRef} from "react";

function UploadDocument({ apiClient }) {
    const inputElement = useRef(null)

    const handleSubmit = async () => {
        await apiClient.uploadDocument(inputElement.current.files[0]);
    };

    return (
        <div>
            <Input id={"nhs-number-input"} label="Enter NHS number"
                   type="text" placeholder="012 345 6789"
            />
            <Input id={"document-input"} label="Choose document"
                   type="file" inputRef={inputElement}
                   multiple={false}
                   />
            <Button onClick={handleSubmit}>
                Upload
            </Button>
        </div>
    )
}

export default UploadDocument