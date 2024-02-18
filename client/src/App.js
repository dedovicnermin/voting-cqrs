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
              </Routes>
            </main>
        </BrowserRouter>
      </div>
  );
}

export default App;
