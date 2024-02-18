import { useAuthContext } from '../services/useAuthContext';
import React, { Component, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useNavigate, useParams } from 'react-router-dom';
// import { useLogout } from '../services/useLogout';

const Header = () => {

  // const {logout} = useLogout();
  // const {user} = useAuthContext();
  const user = {
    usernname: "me"
  };
  const navigate = useNavigate();

  const doLogout = () => {
      // logout();
      console.log('LOGOUT')
  }

  const goToLogin = () => {
      navigate("/login");
  }

  return (
    <header className='bg-secondary bg-gradient'>
      <div className='container'>
        <nav className="navbar">
          {user ? (
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
          ) : (
          <div></div>
          )
        }
          <div className="ml-auto">
            { user ? (
              <div>
                  <span style={{color: "white"}}>{user?.username}</span>
                  <button className="btn btn-link" onClick={doLogout}>
                      <i className="fa fa-sign-out" aria-hidden="true"></i>
                  </button>
              </div>
              ) : (
              <div className="d-flex justify-content-end">
                  <button className="btn btn-link" onClick={goToLogin}>
                      <i className="fa fa-sign-in" aria-hidden="true"></i>
                  </button>
              </div>
              )
            }                      
          </div>
        </nav>
      </div>
    </header>
  );
}
export default Header