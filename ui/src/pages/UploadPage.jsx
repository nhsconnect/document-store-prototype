import { Button, Input } from "nhsuk-react-components";
import React from "react";
import { useForm } from "react-hook-form";

const UploadPage = ({ client }) => {
    const { register, handleSubmit } = useForm();
    const { ref: documentInputRef, ...documentInputProps } =
        register("document");

    const doSubmit = async (data) => {
        await client.uploadDocument(data.document[0]);
    };

    return (
        <>
            <h2>Upload Patient Records</h2>
            <div>
                <form onSubmit={handleSubmit(doSubmit)}>
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
                        multiple={false}
                        name="document"
                        {...documentInputProps}
                        inputRef={documentInputRef}
                    />
                    <Button type="submit">Upload</Button>
                </form>
            </div>
        </>
    );
};

export default UploadPage;
