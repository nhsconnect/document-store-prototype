import {createMemoryHistory} from "history";
import {render, screen, waitFor} from "@testing-library/react";
import * as ReactRouter from "react-router";
import userEvent from "@testing-library/user-event";
import BackButton from "./BackButton";

describe("BackButton", () => {

    it("navigates to previous page when clicking the back button", async () => {
        const history = createMemoryHistory({initialEntries: ["/", "/example"], initialIndex: 1});

        render(
            <ReactRouter.Router navigator={history} location={"/example"}>
                <BackButton/>
            </ReactRouter.Router>
        );

        userEvent.click(screen.queryByText("Back"));

        await waitFor(() => {
            expect(history.location.pathname).toBe("/");
        });
    });

});