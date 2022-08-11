import {createContext} from "react";

const AuthenticationContext = createContext({
    isAuthenticated: false,
    setIsAuthenticated: () => {},
    error: undefined,
    setError: () => {}
});

export default AuthenticationContext;