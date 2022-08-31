import { Button } from "nhsuk-react-components";
import { useNavigate } from "react-router";

const UploadSuccessPage = ({ nextPagePath }) => {
    const navigate = useNavigate();

    const onDone = () => {
        navigate(nextPagePath);
    };

    return (
        <>
            <h2>NHS Digital DocStore</h2>
            <p>File uploaded successfully</p>
            <Button onClick={onDone}>Done</Button>
        </>
    );
};

export default UploadSuccessPage;
