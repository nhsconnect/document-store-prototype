import React from "react";
import Header from "../header/Header";
import "./layout.scss";
import { Footer } from "nhsuk-react-components";

const Layout = ({ children }) => {
    return (
        <div>
            <Header />
            <div
                className="nhsuk-width-container"
                style={{
                    margin: `0 auto`,
                    maxWidth: 960,
                    padding: `0 1.0875rem 1.45rem`,
                    minHeight: "75vh",
                }}
            >
                <main className="nhsuk-main-wrapper app-homepage" id="maincontent" role="main" >
                    <section className="app-homepage-content">
                        <div>{children}</div>
                    </section>
                </main>
            </div>
            <Footer>
                <Footer.Copyright>&copy; {"Crown copyright"}</Footer.Copyright>
            </Footer>
        </div>
    );
};

export default Layout;
