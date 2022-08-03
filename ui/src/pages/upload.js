import * as React from "react"
import UploadDocument from "../components/UploadDocument";

const UploadPage = ({client}) => {
    return (
        <>
            <h2>Upload Patient Records</h2>
            <div>
                <UploadDocument apiClient={client} />
            </div>
        </>
    )
}

export default UploadPage