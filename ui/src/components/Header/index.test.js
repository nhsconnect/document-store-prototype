import React from "react";
import { render, screen } from "@testing-library/react";
import Header from ".";

describe("Header component", () => {
  it("displays site service name", () => {
    render(<Header />);

    expect(screen.queryByText("Document Store")).toBeTruthy();
  });
  it("displays a logout button", () => {
    render(<Header />);

    expect(screen.queryByText("Logout")).toBeTruthy();
  });
});
