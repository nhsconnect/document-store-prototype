import {Button, Input, Table} from "nhsuk-react-components";
import React from "react";
import {useController} from "react-hook-form";
import {formatSize} from "../utils/utils";


const DocumentsInput = ({control}) => {
    const {field: {ref, onChange, onBlur, name, value}, fieldState} = useController({
        name: "documents",
        control,
        rules: {
            validate: {
                isFile: (value) => {
                    return (value && value.length > 0) || "Please select a file"
                },
                isLessThan5GB: (value) =>{
                    for(let i = 0; i < value.length; i++){
                        if(value[i].size > 5 * Math.pow(1024, 3)) {
                            return "Please ensure that all files are less than 5GB in size"
                        }
                    }
                }
            }
        },
    });

    const onRemove = (index) => {
        onChange([
        ...value.slice(0, index),
        ...value.slice(index + 1)
        ])
    }
    return(
        <>
            {/* override the width attribute because the value of the file input is read-only, so when we remove a file it doesn't update */}
            <Input
                id={"documents-input"}
                label="Choose documents"
                type="file"
                multiple={true}
                name={name}
                error={fieldState.error?.message}
                onChange={e => { onChange(Array.from(e.target.files)) }}
                onBlur={onBlur}
                inputRef={ref}
                style={{ width: 133 }}
            />
            {value && value.length > 0 && <Table caption="Documents">
                <Table.Head>
                    <Table.Row>
                        <Table.Cell>Filename</Table.Cell>
                        <Table.Cell>Size</Table.Cell>
                        <Table.Cell>Remove</Table.Cell>
                    </Table.Row>
                </Table.Head>

                <Table.Body>
                    {value.map((document, index) => (
                        <Table.Row key = {document.name}>
                            <Table.Cell>
                                {document.name}
                            </Table.Cell>
                            <Table.Cell>
                                {formatSize(document.size)}
                            </Table.Cell>
                            <Table.Cell>
                                <Button onClick={() => onRemove(index)}>Remove</Button>
                            </Table.Cell>
                        </Table.Row>
                    ))}
                </Table.Body>
            </Table>}
        </>

    )

}
export default DocumentsInput;