import {BackLink} from "nhsuk-react-components";
import React from "react";
import {useNavigate} from "react-router";

const BackButton = () => {

    const navigate = useNavigate();

    const onBack = (e) => {
        e.preventDefault();
        navigate(-1);
    };

    return <BackLink onClick={onBack}>Back</BackLink>;

}

export default BackButton;