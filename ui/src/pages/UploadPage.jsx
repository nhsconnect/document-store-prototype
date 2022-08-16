import { Button, Input, Select } from "nhsuk-react-components";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useFeatureToggle } from "../providers/FeatureToggleProvider";

const states = {
  IDLE: "idle",
  UPLOADING: "uploading",
  SUCCEEDED: "succeeded",
  FAILED: "failed",
};

const UploadPage = ({ client }) => {
  const showMetadataFields = useFeatureToggle(
    "SHOW_METADATA_FIELDS_ON_UPLOAD_PAGE"
  );
  const { register, handleSubmit, formState } = useForm();
  const { ref: documentInputRef, ...documentInputProps } = register(
    "document",
    {
      validate: {
        isFile: (value) => value[0] instanceof File || "Please attach a file",
        isLessThan5GB: (value) =>
          value[0]?.size <= 5 * 107374184 ||
          "File size greater than 5GB - upload a smaller file",
      },
    }
  );
  const { ref: nhsNumberRef, ...nhsNumberProps } = register("nhsNumber");
  const { ref: documentTitleRef, ...documentTitleProps } = register(
    "documentTitle",
    { required: showMetadataFields ? "Please enter document title" : false }
  );
  const { ref: clinicalCodeRef, ...clinicalCodeProps } =
    register("clinicalCode");
  const [submissionState, setSubmissionState] = useState(states.IDLE);

  const doSubmit = async (data) => {
    try {
      setSubmissionState(states.UPLOADING);
      const documentTitle = showMetadataFields
        ? data.documentTitle
        : "Jane Doe - Patient Record";
      const clinicalCode = showMetadataFields
        ? data.clinicalCode
        : "22151000087106";

      await client.uploadDocument(
        data.document[0],
        data.nhsNumber,
        documentTitle,
        clinicalCode
      );
      setSubmissionState(states.SUCCEEDED);
    } catch (e) {
      setSubmissionState(states.FAILED);
    }
  };

  return (
    <>
      <h2>Upload Patient Records</h2>
      <div>
        <form onSubmit={handleSubmit(doSubmit)} noValidate>
          <Input
            id={"nhs-number-input"}
            label="Enter NHS number"
            name="nhsNumber"
            type="text"
            {...nhsNumberProps}
            inputRef={nhsNumberRef}
          />
          {showMetadataFields && (
            <>
              <Input
                id={"document-title-input"}
                label="Enter Document Title"
                type="text"
                name="documentTitle"
                placeholder="Document Title"
                error={formState.errors.documentTitle?.message}
                {...documentTitleProps}
                inputRef={documentTitleRef}
              />
              <Select
                name="clinicalCode"
                label="Select Clinical Code"
                {...clinicalCodeProps}
                selectRef={clinicalCodeRef}
              >
                <Select.Option value="22151000087106" defaultValue>
                  Paper Report (Record Artifact)
                </Select.Option>
              </Select>
            </>
          )}
          <Input
            id={"document-input"}
            label="Choose document"
            type="file"
            multiple={false}
            name="document"
            error={formState.errors.document?.message}
            {...documentInputProps}
            inputRef={documentInputRef}
          />
          <Button type="submit">Upload</Button>
          {submissionState === states.UPLOADING && (
            <p>
              <progress aria-label={"Loading..."} />
            </p>
          )}
          {submissionState === states.SUCCEEDED && (
            <p>Document uploaded successfully</p>
          )}
          {submissionState === states.FAILED && (
            <p>File upload failed - please retry</p>
          )}
        </form>
      </div>
    </>
  );
};

export default UploadPage;
