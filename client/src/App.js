import './App.css';
import 'bootstrap/dist/css/bootstrap.min.css'
import { Routes, Route, Navigate } from "react-router-dom";
import Header from './components/Header'
import AllElections from './components/AllElections'
import MyElections from './components/MyElections'
import CreateElection from './components/CreateElection';
import ElectionView from './components/ElectionView';
import {useEffect, useReducer} from "react";
import AppReducer, {ELECTION_EVENTS} from "./context/reducer";
import {useResource} from "react-request-hook";
import {StateContext} from "./context/context";
import ElectionList from "./component/ElectionList";
import Election from "./component/election/Election";
import LoginOrRegister from "./component/user/LoginOrRegister";


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
      const refresh = setInterval(() => {getElections()}, 5000)
      return () => clearInterval(refresh)
    }
  }, [state?.user?.token, state?.user?.id, state?.user?.username]);

  useEffect(() => {
    if (electionsResp?.data && electionsResp.isLoading === false) {
      dispatch({ type: ELECTION_EVENTS.FETCH_ELECTIONS, payload: electionsResp.data })
    }
  }, [electionsResp]);


  const user = true;

  return(
      <div className="App">
        <StateContext.Provider value={{state, dispatch}}>
          <Header/>
            <main>
              <Routes>
                {/*<Route path = "/" element={user ? <AllElections/> : <Navigate to="/login"/>}/>*/}
                <Route path = "/" element={state.user?.id ? <Navigate to="/elections"/> : <Navigate to="/login"/>}/>
                <Route path = "/all-elections" element = {user ? <AllElections/> : <Navigate to = "/login"/>}/>
                <Route path = "/my-elections" element = {user ? <MyElections/> : <Navigate to = "/login"/>}/>
                <Route path = "/create" element = {user ? <CreateElection/> : <Navigate to = "/login"/>}/>
                {/*<Route path = "/login" element={!user ? <LoginPage/> : <Navigate to={"/"}/>}/>*/}
                <Route path = "/all-elections/:id" element = {user ? <ElectionView/> : <Navigate to = "/login"/>}/>

                <Route path = "/login" element={state.user?.id ? <Navigate to="/elections"/> : <LoginOrRegister/> } />
                <Route path = "/elections" element={state.user?.id ? <ElectionList/> : <Navigate to="/login"/>}/>
                <Route path = "/elections/:id" element={state.user?.id ? <Election /> : <Navigate to="/login"/> }/>
              </Routes>
            </main>
        </StateContext.Provider>
      </div>
  );
}

export default App;
