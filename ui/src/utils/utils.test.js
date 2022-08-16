import * as utils from "./utils";

describe("setUrlHostToLocalHost utility", () => {
  let env = "";
  beforeAll(() => {
    env = process.env.NODE_ENV;
  });
  afterAll(() => {
    process.env.NODE_ENV = env;
  });
  test("change url host to local host if in the development environment", () => {
    process.env.NODE_ENV = "development";
    const url = "http://host:1234/test";
    const updatedUrl = utils.setUrlHostToLocalHost(url);
    expect(updatedUrl).toBe("http://localhost:1234/test");
  });
  test("not change url host to local host if not in the development environment", () => {
    process.env.NODE_ENV = "production";
    const url = "http://host:1234/test";
    const updatedUrl = utils.setUrlHostToLocalHost(url);
    expect(updatedUrl).toBe("http://host:1234/test");
  });
});
