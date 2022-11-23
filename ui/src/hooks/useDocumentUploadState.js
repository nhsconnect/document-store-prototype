import { useState } from "react";
import { produce } from "immer"
import { documentUploadStates as stateNames, documentUploadSteps } from "../enums/documentUploads";

export default () => {
    const [documentUploadStates, setDocumentUploadStates] = useState([]);

    const onUploadStateChange = (documentIndex, state, progress) => {
        setDocumentUploadStates(current => {
            return produce(current, draft => {
                draft[documentIndex] = {
                    state,
                    progress
                }
            })
        })
    }

    const uploadStep = () => {
        if (documentUploadStates.length === 0) {
            return documentUploadSteps.SELECTING_FILES
        }

        if (documentUploadStates.every((uploadState) => {
            return (uploadState.state === stateNames.SUCCEEDED || uploadState.state === stateNames.FAILED)
        })) {
            return documentUploadSteps.COMPLETE
        }

        return documentUploadSteps.UPLOADING
    }

    return { documentUploadStates, uploadStep: uploadStep(), onUploadStateChange }
}