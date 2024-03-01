import './App.css';
import '../node_modules/bootstrap/dist/css/bootstrap.css'
import { useAuthContext } from './services/useAuthContext';
import { BrowserRouter, Routes, Route, Navigate} from "react-router-dom";
import Header from './components/Header'
import AllElections from './components/AllElections'
import MyElections from './components/MyElections'
import CreateElection from './components/CreateElection';
import LoginPage from './components/LoginPage';
import ElectionView from './components/ElectionView';
<<<<<<< Updated upstream
=======
import {useEffect, useReducer} from "react";
import AppReducer, {ELECTION_EVENTS} from "./context/reducer";
import {useResource} from "react-request-hook";
import {StateContext} from "./context/context";
import ElectionList from "./component/ElectionList";
import Election from "./component/election/Election";
import Login from './components/Login';
import Register from './components/Register';
// import LoginOrRegister from "./component/user/LoginOrRegister";
>>>>>>> Stashed changes


const App = () => {

  // const {user} = useAuthContext();
  const user = true;

  return(
      <div className="App">
        <BrowserRouter>
          <Header/>
            <main>
              <Routes>
                <Route path = "/" element={user ? <AllElections/> : <Navigate to="/login"/>}/>
                <Route path = "/all-elections" element = {user ? <AllElections/> : <Navigate to = "/login"/>}/>
                <Route path = "/my-elections" element = {user ? <MyElections/> : <Navigate to = "/login"/>}/>
                <Route path = "/create" element = {user ? <CreateElection/> : <Navigate to = "/login"/>}/>
                <Route path = "/login" element={!user ? <LoginPage/> : <Navigate to={"/"}/>}/>
                <Route path = "/all-elections/:id" element = {user ? <ElectionView/> : <Navigate to = "/login"/>}/>
<<<<<<< Updated upstream
=======
                <Route path = "/elections" element={state.user?.id ? <ElectionList/> : <Navigate to="/login"/>}/>
                <Route path = "/elections/:id" element={state.user?.id ? <Election /> : <Navigate to="/login"/> }/>

                {/* <Route path = "/login" element={state.user?.id ? <Navigate to="/elections"/> : <LoginOrRegister/> } /> */}
                <Route path = "/login" element={user?.id ? <Navigate to="/elections"/> : <Login/> }/>
                <Route path = "/register" element={user?.id ? <Navigate to="/elections"/> : <Register/> }/>
>>>>>>> Stashed changes
              </Routes>
            </main>
        </BrowserRouter>
      </div>
  );
}

export default App;
