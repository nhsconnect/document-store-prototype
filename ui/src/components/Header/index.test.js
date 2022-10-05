import React from "react";
import { render, screen } from "@testing-library/react";
import Header from ".";

const mockNavigate = jest.fn();
 jest.mock("react-router",() => ({
 ...jest.requireActual('react-router'),
     useNavigate: ()=> mockNavigate
 }));

describe("Header component", () => {
  it("displays site service name", () => {
    render(<Header />);
    expect(screen.queryByText("Document Store")).toBeTruthy();
  });
});
