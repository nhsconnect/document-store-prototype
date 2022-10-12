import {BackLink} from "nhsuk-react-components";
import React from "react";
// import {useNavigate} from "react-router";
import Link from 'next/link'

const BackButton = ({ href }) => {


    return <BackLink><Link href={href}>Back</Link></BackLink>;

}

export default BackButton;