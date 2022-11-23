import { renderHook, act } from "@testing-library/react-hooks";
import { documentUploadStates, documentUploadSteps } from "../enums/documentUploads";
import useDocumentUploadState from "./useDocumentUploadState";

describe('The useDocumentUploadState custom react hook', () => {
    it('returns the expected initial values', () => {
        const { result } = renderHook(()=> useDocumentUploadState());
        expect(result.current.documentUploadStates instanceof Array).toBe(true);
        expect(result.current.documentUploadStates).toHaveLength(0);
        expect(result.current.uploadStep).toBe(documentUploadSteps.SELECTING_FILES);
    })

    it('updates the document upload states and step when the state change handler is called.', () => {
        const { result } = renderHook(()=> useDocumentUploadState());

        const makeTestCase = (index, state, progress) => ({index, state, progress})

        const testCases = [
            makeTestCase(0, documentUploadStates.WAITING, 0),
            makeTestCase(1, documentUploadStates.WAITING, 0),
            makeTestCase(1, documentUploadStates.UPLOADING, 10),
            makeTestCase(2, documentUploadStates.UPLOADING, 20),
            makeTestCase(1, documentUploadStates.SUCCEEDED, 100),
            makeTestCase(0, documentUploadStates.FAILED, 0),
            makeTestCase(2, documentUploadStates.SUCCEEDED, 100),
        ]

        expect(result.current.uploadStep).toBe(documentUploadSteps.SELECTING_FILES);
        
        testCases.forEach((testCase, caseIndex) => {
            const { index, state, progress } = testCase
            act(() => {
                result.current.onUploadStateChange(index, state, progress)
            })
            const { state: resultState, progress: resultProgress } = result.current.documentUploadStates[index]

            expect(state).toEqual(resultState)
            expect(progress).toEqual(resultProgress)

            if (caseIndex < testCases.length - 1) {
                expect(result.current.uploadStep).toBe(documentUploadSteps.UPLOADING);
            } else {
                expect(result.current.uploadStep).toBe(documentUploadSteps.COMPLETE);
            }
        })
    })

})