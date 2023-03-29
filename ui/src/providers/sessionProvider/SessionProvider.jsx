import { createContext, useContext, useState } from "react";

const SessionContext = createContext(null);

const SessionProvider = ({ children }) => {
    const sessionState = useState({
        isLoggedIn: false,
    });

    return <SessionContext.Provider value={sessionState}>{children}</SessionContext.Provider>;
};

export default SessionProvider;
export const useSessionContext = () => useContext(SessionContext);
