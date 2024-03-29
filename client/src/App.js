import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css'
import { Routes, Route, Navigate } from "react-router-dom";
import {useEffect, useReducer} from "react";
import AppReducer, {ELECTION_EVENTS} from "./context/reducer";
import {useResource} from "react-request-hook";
import {StateContext} from "./context/context";

import Header from './components/Header'
import ElectionList from "./component/ElectionList";
import Election from "./component/election/Election";
import MyElectionList from './component/MyElectionList'
import CreateElection from './components/CreateElection';
import Login from './components/Login';
import Register from './components/Register';

const App = () => {

  const [state, dispatch] = useReducer(AppReducer, {
    user: {},
    elections: []
  })

  const [electionsResp, getElections] = useResource(() => ({
    url: "/elections",
    method: "get",
    headers: { Authorization: `Bearer ${state?.user?.token}`}
  }))

  useEffect(() => {
    if (state.user.token && state.user.username && state.user.id) {
      getElections()
      const refresh = setInterval(() => {getElections()}, 2000)
      return () => clearInterval(refresh)
    }
  }, [state?.user?.token, state?.user?.id, state?.user?.username]);

  useEffect(() => {
    if (electionsResp?.data && electionsResp.isLoading === false) {
      dispatch({ type: ELECTION_EVENTS.FETCH_ELECTIONS, payload: electionsResp.data })
    }
  }, [electionsResp]);

  return(
      <div className="App">
        <StateContext.Provider value={{state, dispatch}}>
          <Header/>
            <main>
              <Routes>
                <Route path = "/" element={state.user?.id ? <Navigate to="/elections"/> : <Navigate to="/login"/>}/>
                <Route path = "/elections" element={state.user?.id ? <ElectionList/> : <Navigate to="/login"/>}/>
                <Route path = "/elections/:id" element={state.user?.id ? <Election/> : <Navigate to="/login"/> }/>
                <Route path = "/my-elections" element={state.user?.id ? <MyElectionList/> : <Navigate to="/login"/>}/>
                <Route path = "/create" element={state.user?.id ? <CreateElection/> : <Navigate to="/login"/>}/>
                <Route path = "/login" element={state.user?.id ? <Navigate to="/elections"/> : <Login/> }/>
                <Route path = "/register" element={state.user?.id ? <Navigate to="/elections"/> : <Register/> }/>
              </Routes>
            </main>
        </StateContext.Provider>
      </div>
  );
}

export default App;
