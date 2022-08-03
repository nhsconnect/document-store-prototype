import React from "react";
import { render, screen } from "@testing-library/react";
import Header from "..";

describe("Header component", () => {
  it("displays site service name", () => {
    render(<Header />);

    expect(screen.queryByText("Practice Migration Data")).toBeTruthy();
  });
});
