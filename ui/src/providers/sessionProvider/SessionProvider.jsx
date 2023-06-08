import { createContext, useContext, useEffect, useState } from "react";

const SessionContext = createContext(null);

const SessionProvider = ({ children }) => {
    const [session, setSession] = useState({
        isLoggedIn: sessionStorage.getItem("LoggedIn") === "true",
        userRole: sessionStorage.getItem("UserRole") ?? null,
    });

    const deleteSession = () => {
        setSession({
            isLoggedIn: false,
            userRole: null,
        });
    };

    useEffect(() => {
        sessionStorage.setItem("LoggedIn", session.isLoggedIn ? "true" : "false");
        sessionStorage.setItem("UserRole", session.userRole ?? null);
    }, [session]);

    return <SessionContext.Provider value={[session, setSession, deleteSession]}>{children}</SessionContext.Provider>;
};

export default SessionProvider;
export const useSessionContext = () => useContext(SessionContext);
