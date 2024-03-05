export const USER_EVENTS = {
    LOGIN: "login",
    LOGOUT: "logout",
    REGISTER: "register"
}

export const ELECTION_EVENTS = {
    CREATE_ELECTION: "create_election",
    FETCH_ELECTIONS: "fetch_elections"
}

function userDispatch(user, action) {
    switch (action.type) {
        case USER_EVENTS.LOGIN:
        case USER_EVENTS.REGISTER:
            return action.payload;
        case USER_EVENTS.LOGOUT:
            return {};
        default:
            return user;
    }
}

function electionDispatch(elections, action) {
    switch (action.type) {
        case ELECTION_EVENTS.FETCH_ELECTIONS:
            return action.payload;
        default:
            return elections;
    }
}

export default function AppReducer(state, action) {
    return {
        user: userDispatch(state.user, action),
        elections: electionDispatch(state.elections, action)
    };
}
