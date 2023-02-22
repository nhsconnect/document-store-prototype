import { downloadFile, formatSize, getFormattedDate, setUrlHostToLocalHost } from "./utils";

describe("utils", () => {
    describe("setUrlHostToLocalHost()", () => {
        const nodeEnv = process.env.NODE_ENV;

        afterAll(() => {
            process.env.NODE_ENV = nodeEnv;
        });

        it("changes URL host to localhost if in the development env", () => {
            process.env.NODE_ENV = "development";
            const url = "https://host:1234/test";

            const updatedUrl = setUrlHostToLocalHost(url);

            expect(updatedUrl).toBe("https://localhost:1234/test");
        });

        it("does not change URL host to localhost if not in the development env", () => {
            process.env.NODE_ENV = "production";
            const url = "https://host:1234/test";

            const updatedUrl = setUrlHostToLocalHost(url);

            expect(updatedUrl).toBe("https://host:1234/test");
        });
    });

    describe("formatSize()", () => {
        it.each([
            [1023456, "999 KB"],
            [1023456000, "976 MB"],
            [1000000000000, "931 GB"],
            [0, "0 bytes"],
        ])("converts %s bytes to appropriate storage unit of %s upto GB", (bytes, expectedConversion) => {
            expect(formatSize(bytes)).toEqual(expectedConversion);
        });

        it("throws an error if the size is less than zero", () => {
            expect(() => formatSize(-1)).toThrow();
        });

        it("throws an error if file size is greater than a TB", () => {
            expect(() => formatSize(10000000000000)).toThrow();
        });
    });

    describe("downloadFile()", () => {
        const createElementFn = document.createElement;

        afterEach(() => {
            Object.defineProperty(document, "createElement", { value: createElementFn, configurable: true });
        });

        it("creates and clicks a link once", () => {
            const link = { click: jest.fn() };

            Object.defineProperty(document, "createElement", { value: () => link, configurable: true });
            downloadFile("some-url", "some-filename");

            expect(link.click).toHaveBeenCalledTimes(1);
        });

        it("creates a link with a given URL", () => {
            const link = { click: jest.fn() };
            const url = "some-url";

            Object.defineProperty(document, "createElement", { value: () => link, configurable: true });
            downloadFile(url, "some-filename");

            expect(link.href).toEqual(url);
        });

        it("creates a link with the to be downloaded filename", () => {
            const link = { click: jest.fn() };
            const filename = "some-filename";

            Object.defineProperty(document, "createElement", { value: () => link, configurable: true });
            downloadFile("some-url", filename);

            expect(link.download).toEqual(filename);
        });
    });

    describe("getFormattedDate()", () => {
        it("returns 'Invalid date' if date is null", () => {
            expect(getFormattedDate(null)).toEqual("Invalid date");
        });

        it("returns 'Invalid date' if date is not valid", () => {
            expect(getFormattedDate("201405")).toEqual("Invalid date");
        });

        it("returns correctly formatted date when date is valid", () => {
            expect(getFormattedDate("20001020")).toEqual("20 October 2000");
        });
    });
});
