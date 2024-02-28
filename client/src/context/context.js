import { createContext } from "react";

export const StateContext = createContext({
    state: {},
    dispatch: () => {}
});