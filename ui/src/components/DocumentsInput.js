import {Button, Input, Table} from "nhsuk-react-components";
import React from "react";
import {useController} from "react-hook-form";


const DocumentsInput = ({control}) => {
    const {field: {ref, onChange, onBlur, name, value}, fieldState} = useController({
        name:"documents",
        control,
        rules: { required: true },
    });
    return(
        <>
            <Input
                id={"documents-input"}
                label="Choose documents"
                type="file"
                multiple={true}
                name={name}
                // error={fieldState.error?.message}
                onChange={e => { onChange(e.target.files) }}
                onBlur={onBlur}
                inputRef={ref}
            />
            {value && value.length > 0 && <Table caption="Documents">
                <Table.Head>
                    <Table.Row>
                        <Table.Cell>Filename</Table.Cell>
                        <Table.Cell>Uploaded At</Table.Cell>
                        <Table.Cell>Download</Table.Cell>
                    </Table.Row>
                </Table.Head>

                <Table.Body>
                    {Array.from(value).map((document) => (
                        <Table.Row key = {document.name}>
                            <Table.Cell>
                                {document.name}
                            </Table.Cell>
                            {/*<Table.Cell style={alignMiddle}>*/}
                            {/*    {document.size}*/}
                            {/*</Table.Cell>*/}
                            {/*<Table.Cell style={alignMiddle}>*/}

                            {/*</Table.Cell>*/}
                        </Table.Row>
                    ))}
                </Table.Body>
            </Table>}
        </>

    )

}
export default DocumentsInput;