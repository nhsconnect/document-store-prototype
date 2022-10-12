import {API, Auth} from "aws-amplify";
import ApiClient from "../apiClients/apiClient";

const PatientTraceResultsPage = ({ result }) => {
    console.log(result)
    return <div>Hello</div>
}

export const getServerSideProps = async ({ req, query }) => {
    const client = new ApiClient(API, Auth);
    try {
        const res = await fetch('https://ipapi.co/8.8.8.8/json');
        const patientData = await res.json();
        console.log(patientData)
    } catch (e) {
        console.log("error: %s", e)
    }
    return {
        props: {
            result: patientData?.reason
        }
    }
}

export default PatientTraceResultsPage