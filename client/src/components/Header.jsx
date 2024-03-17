import React, { useContext} from 'react';
import {StateContext} from "../context/context";
import { Link, useNavigate } from 'react-router-dom';
import {USER_EVENTS} from "./../context/reducer";

const Header = () => {

  const {dispatch} = useContext(StateContext);
  const {state} = useContext(StateContext)
  const navigate = useNavigate();

  const doLogout = () => {
    dispatch({
      type: USER_EVENTS.LOGOUT,
      payload: {}
  });
  }

  const goToLogin = () => {
      navigate("/login");
  }

  if (state.user?.id) {
    return (
      <header className='bg-secondary bg-gradient'>
        <div className='container'>
          <nav className="navbar">
            <div>
              <ul className="nav">
                <li>
                  <Link className="nav-link" to="/elections">Elections</Link>
                </li>
                <li>
                  <Link className="nav-link" to="/my-elections">My Elections</Link>
                </li>
                <li>
                  <Link className="nav-link" to="/create">Create new</Link>
                </li>
              </ul>
            </div>
            <div className="ml-auto">
              <div className="d-flex justify-content-end">
                <ul className="nav red">
                  <li>
                    <button className="nav-link" onClick={doLogout}>Logout</button>
                  </li>
                </ul>
              </div>
            </div>              
          </nav>
        </div>
      </header>
    );
  }
  else {
    return (
      <div style={{color: "rgba(0, 0, 0, 0)"}}>Aweful workaround to hide the header</div>
    );
  }
}

export default Header