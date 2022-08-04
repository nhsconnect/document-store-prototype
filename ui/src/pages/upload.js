import { Button, Input } from "nhsuk-react-components";
import React, { useRef } from "react";

const UploadPage = ({ client }) => {
    const inputElement = useRef(null);

    const handleSubmit = async (event) => {
        event.preventDefault();
        await client.uploadDocument(inputElement.current.files[0]);
    };

    return (
        <>
            <h2>Upload Patient Records</h2>
            <div>
                <form onSubmit={handleSubmit}>
                    <Input
                        id={"nhs-number-input"}
                        label="Enter NHS number"
                        type="text"
                        placeholder="012 345 6789"
                    />
                    <Input
                        id={"document-input"}
                        label="Choose document"
                        type="file"
                        inputRef={inputElement}
                        multiple={false}
                    />
                    <Button type="submit">Upload</Button>
                </form>
            </div>
        </>
    );
};

export default UploadPage;
