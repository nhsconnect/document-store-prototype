import { nanoid } from "nanoid/non-secure";
import { Input, Table, WarningCallout } from "nhsuk-react-components";
import React, { useRef } from "react";
import { useController } from "react-hook-form";
import { documentUploadStates } from "../../enums/documentUploads";
import { fileSizes } from "../../enums/fileSizes";
import { formatSize, toFileList } from "../../utils/utils";

const DocumentsInput = ({ control }) => {
    const inputRef = useRef(null);
    const {
        field: { ref, onChange, onBlur, name, value },
        fieldState,
    } = useController({
        name: "documents",
        control,
        rules: {
            validate: {
                isFile: (value) => {
                    return (value && value.length > 0) || "Please select a file";
                },
                isLessThan5GB: (value) => {
                    for (let i = 0; i < value.length; i++) {
                        if (value[i].file.size > fileSizes.FIVE_GIGA_BYTES) {
                            return "Please ensure that all files are less than 5GB in size";
                        }
                    }
                },
            },
        },
    });

    const onRemove = (index) => {
        const updatedValues = [...value.slice(0, index), ...value.slice(index + 1)];
        onChange(updatedValues);

        // Horrible hack to update input value so that it removes the file from its selection
        // Otherwise, we cannot add a file, remove it and then add it again, as the input doesn't know its value has changed
        inputRef.current.files = toFileList(updatedValues.map((value) => value.file));
    };

    const hasDuplicateFiles =
        value &&
        value.some((document) => {
            return value.some(
                (comparison) => document.file.name === comparison.file.name && document.id !== comparison.id
            );
        });

    const changeHandler = (event) => {
        const newFiles = event.target.files instanceof Array ? event.target.files : Array.from(event.target.files);
        const newDocumentObjects = newFiles.map((file) => ({
            id: nanoid(),
            file,
            state: documentUploadStates.SELECTED,
            progress: 0,
        }));

        onChange(value ? value.concat(newDocumentObjects) : newDocumentObjects);
    };

    const mergedRefs = (value) => {
        inputRef.current = value;
        ref(value);
    };

    return (
        <>
            {/* TODO: Override the width attribute because the value of the file input is read-only, so when we remove a file it doesn't update */}
            <Input
                id="documents-input"
                label="Select file(s)"
                hint={
                    <ul>
                        <li>{"A patient's full electronic health record including attachments must be uploaded."}</li>
                        <li>{"You can select multiple files to upload at once."}</li>
                        <li>
                            In the event documents cannot be uploaded, they must be printed and sent via{" "}
                            <a href="https://secure.pcse.england.nhs.uk/" target="_blank" rel="noreferrer">
                                Primary Care Support England
                            </a>
                            .
                        </li>
                    </ul>
                }
                type="file"
                multiple={true}
                name={name}
                error={fieldState.error?.message}
                onChange={changeHandler}
                onBlur={onBlur}
                inputRef={mergedRefs}
            />
            <div role="region" aria-live="polite">
                {value && value.length > 0 && (
                    <Table caption="Selected documents">
                        <Table.Head>
                            <Table.Row>
                                <Table.Cell>Filename</Table.Cell>
                                <Table.Cell>Size</Table.Cell>
                                <Table.Cell>Remove</Table.Cell>
                            </Table.Row>
                        </Table.Head>

                        <Table.Body>
                            {value.map((document, index) => (
                                <Table.Row key={document.id}>
                                    <Table.Cell>{document.file.name}</Table.Cell>
                                    <Table.Cell>{formatSize(document.file.size)}</Table.Cell>
                                    <Table.Cell>
                                        <a
                                            href="#"
                                            aria-label={`Remove ${document.file.name} from selection`}
                                            onClick={() => onRemove(index)}
                                        >
                                            Remove
                                        </a>
                                    </Table.Cell>
                                </Table.Row>
                            ))}
                        </Table.Body>
                    </Table>
                )}
                {hasDuplicateFiles && (
                    <WarningCallout>
                        <WarningCallout.Label>Possible duplicate file</WarningCallout.Label>
                        <p>There are two or more documents with the same name.</p>
                        <p>Are you sure you want to proceed?</p>
                    </WarningCallout>
                )}
            </div>
        </>
    );
};
export default DocumentsInput;
