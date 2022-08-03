import React from "react";

import Header from "./Header";
import "./layout.scss";
import { Footer, Hero } from "nhsuk-react-components";
import { PhaseBanner } from "./PhaseBanner";


const Layout = ({ children }) => {
  return (
    <>
      <Header />
      <PhaseBanner tag={"ALPHA"}>
        {"This is a new site. Content and structure are subject to change."}
      </PhaseBanner>
      <Hero>
        <Hero.Heading data-testid="hero-heading">{"Document Store Prototype"}</Hero.Heading>
        <Hero.Text>{"Platform for uploading and retrieving documents"}</Hero.Text>
      </Hero>
      <div
        style={{
          margin: `0 auto`,
          maxWidth: 960,
          padding: `0 1.0875rem 1.45rem`,
        }}
      >
        <main className="nhsuk-main-wrapper app-homepage" id="maincontent">
          <section className="app-homepage-content">
            <div className="nhsuk-width-container">{children}</div>
          </section>
        </main>
      </div>
      <Footer>
        <Footer.Copyright>&copy; {"Crown copyright"}</Footer.Copyright>
      </Footer>
    </>
  );
};

export default Layout;
