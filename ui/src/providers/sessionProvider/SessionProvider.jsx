import { createContext, useContext, useEffect, useState } from "react";

const SessionContext = createContext(null);

const SessionProvider = ({ children }) => {
    const [session, setSession] = useState({
        isLoggedIn: sessionStorage.getItem("LoggedIn") === "true",
        sessionId: sessionStorage.getItem("SessionId") ?? "",
        subjectClaim: sessionStorage.getItem("SubjectClaim") ?? "",
    });

    useEffect(() => {
        sessionStorage.setItem("LoggedIn", session.isLoggedIn ? "true" : "false");
        sessionStorage.setItem("SessionId", session.sessionId ?? "");
        sessionStorage.setItem("SubjectClaim", session.subjectClaim ?? "");
    }, [session]);

    return <SessionContext.Provider value={[session, setSession]}>{children}</SessionContext.Provider>;
};

export default SessionProvider;
export const useSessionContext = () => useContext(SessionContext);
