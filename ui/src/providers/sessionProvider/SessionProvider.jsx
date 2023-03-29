import { createContext, useContext, useEffect, useState } from "react";

const SessionContext = createContext(null);

const SessionProvider = ({ children }) => {
    const [session, setSession] = useState({ isLoggedIn: sessionStorage.getItem("LoggedIn") === "true" });

    useEffect(() => {
        if (session.isLoggedIn) {
            sessionStorage.setItem("LoggedIn", "true");
        }
    }, [session]);

    return <SessionContext.Provider value={[session, setSession]}>{children}</SessionContext.Provider>;
};

export default SessionProvider;
export const useSessionContext = () => useContext(SessionContext);
