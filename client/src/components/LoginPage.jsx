import { useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
// import { useLogin } from "../hooks/useLogin";

const LoginPage = () => {
    const navigate = useNavigate();
    // const {login, errorMsg, isLoading} = useLogin();
    const login = () => {return true}
    const errorMsg = 'eeeerrrorrr';
    const isLoading = false;
    const [usr, setUsr] = useState("");
    const [pswd, setPswd] = useState("");


    const doSubmit = async (event) => {
        // event.preventDefault();
        // await login(usr, pswd);
        // setUsr("");
        // setPswd("");
        // navigate("/");
        console.log('Mock login request');
    }

    return (

        <div className="signin container d-flex justify-content-center">
            <div>
                <div className="card mt-5">
                    
                    <form className="card-body m-3" onSubmit={doSubmit}>
                        <div className="row text-center">
                        <h3 className="card-title mb-3 font-weight-normal">Sign In</h3>
                        </div>

                        <div className="row">
                            <label htmlFor="username">Username</label>
                            <input className="form-control mb-3" 
                                type="text" 
                                id="username" 
                                autoComplete="off"
                                required
                                onChange={(e) => setUsr(e.target.value)}
                                value={usr}
                            />
                        </div>

                        <div className="row">
                            <label htmlFor="inputPassword">Password</label>
                            <input className="form-control mb-5"
                                type="password" 
                                id="inputPassword"  
                                required
                                onChange={(e) => setPswd(e.target.value)}
                                value={pswd}
                            />
                        </div>
                        <div className="row ">
                            <button disabled={isLoading} className="btn btn-primary" type="submit">Sign In</button>
                        </div>
                    </form>
                </div>
                <div className="text-center">
                    {<div className="error">{errorMsg}</div>}
                </div>
            </div>
        </div>
    );
}

export default LoginPage;