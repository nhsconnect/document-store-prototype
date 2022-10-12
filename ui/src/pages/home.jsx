import React, { useRef } from "react";
import { Button, Fieldset, Radios } from "nhsuk-react-components";
import BackButton from "../components/BackButton";
import { withSSRContext } from "aws-amplify";
import { useRouter } from "next/router";

const Home = () => {
    const downloadRef = useRef(null)
    const uploadRef = useRef(null)
    const router = useRouter()
    const handleSubmit = (e) => {
        e.preventDefault()
        if (downloadRef.current.checked) {
            router.push('/PatientTracePage?trx=download')
        }
        if (uploadRef.current.checked) {
            router.push('/PatientTracePage?trx=upload')
        }
    }
    return (
        <>
            <BackButton href={"/start"}/>
            <form action="/PatientTracePage" method="GET" onSubmit={handleSubmit}>
                <Fieldset>
                    <Fieldset.Legend headingLevel={"h1"} isPageHeading>
                        How do you want to use the Document Store?
                    </Fieldset.Legend>
                    <Radios
                        name="trx"
                        hint="Select an option"
                    >
                        <Radios.Radio
                            id="download"
                            value="download"
                            inputRef={downloadRef}
                        >
                            Download and view a stored document
                        </Radios.Radio>
                        <Radios.Radio
                            id="upload"
                            value="upload"
                            inputRef={uploadRef}
                        >
                            Upload a document
                        </Radios.Radio>
                    </Radios>
                </Fieldset>
                <Button type={"submit"}>Continue</Button>
            </form>
        </>
    );
};

export const getServerSideProps = async ({ req }) => {
    const { Auth } = withSSRContext({ req })

    // try {
    //     await Auth.currentSession()
    //     return
    // } catch (e) {
    //     console.log(e)
    //     return {
    //         redirect: {
    //             statusCode: 302,
    //             location: `https://doc-store-user-pool.auth.eu-west-2.amazoncognito.com/oauth`
    //         }
    //     }
    // }
    return { props: {}}
}

export default Home;
