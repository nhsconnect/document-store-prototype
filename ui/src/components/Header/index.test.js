import React from "react";
import { render, screen } from "@testing-library/react";
import Header from ".";
import {MemoryRouter, useNavigate} from "react-router";
import userEvent from "@testing-library/user-event";

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
  it("displays a logout button when user logged in", () => {
    render(<Header />);

    expect(screen.queryByText("Log Out")).toBeTruthy();
  });
  it("redirect to StartPage when user clicks logout button", () => {
    render(
      <MemoryRouter >
        <Header />
      </MemoryRouter>);
    userEvent.click(screen.getByRole("button", { name: "Log Out" }));
    expect(mockNavigate).toHaveBeenCalledWith("/");
  });
});
