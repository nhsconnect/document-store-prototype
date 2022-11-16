import {Button, Input, Table} from "nhsuk-react-components";
import React from "react";
import {useController} from "react-hook-form";
import {formatSize} from "../utils/utils";


const DocumentsInput = ({control}) => {
    const {field: {ref, onChange, onBlur, name, value}, fieldState, formState} = useController({
        name: "documents",
        control,
        rules: {
            validate: {
                isFile: (value) => {
                    return (value && value.length > 0) || "Please select a file"
                },
                isLessThan5GB: (value) =>{
                    for(let i = 0; i < value.length; i++){
                        if(value.item(i).size > 5 * Math.pow(1024, 3)) {
                            return "Please ensure that all files are less than 5GB in size"
                        }
                    }
                }
            }
        },
    });
    return(
        <>
            <Input
                id={"documents-input"}
                label="Choose documents"
                type="file"
                multiple={true}
                name={name}
                error={fieldState.error?.message}
                onChange={e => { onChange(e.target.files) }}
                onBlur={onBlur}
                inputRef={ref}
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
                    {Array.from(value).map((document) => (
                        <Table.Row key = {document.name}>
                            <Table.Cell>
                                {document.name}
                            </Table.Cell>
                            <Table.Cell>
                                {formatSize(document.size)}
                            </Table.Cell>
                            <Table.Cell>

                            </Table.Cell>a
                        </Table.Row>
                    ))}
                </Table.Body>
            </Table>}
        </>

    )

}
export default DocumentsInput;