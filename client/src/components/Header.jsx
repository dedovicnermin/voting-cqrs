import { useAuthContext } from '../services/useAuthContext';
import React, { Component, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useLogout } from '../services/useLogout';

const Header = () => {

  // const {logout} = useLogout();
  // const {user} = useAuthContext();
  // const user = {
  //   usernname: "me",
  //   id: 11
  // };
  const user = true
  // const user = false
  const navigate = useNavigate();

  const doLogout = () => {
      // logout();
      console.log('LOGOUT')
  }

  const goToLogin = () => {
      navigate("/login");
  }

  if (user) {
    return (
      <header className='bg-secondary bg-gradient'>
        <div className='container'>
          <nav className="navbar">
            <div>
              <ul className="nav">
                <li>
                  <Link className="nav-link" to="/all-elections">Open Elections</Link>
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
                <ul className="nav">
                  <li>
                    <button className="nav-link" onClick={doLogout}></button>
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