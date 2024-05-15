import {useContext, useEffect, useState} from "react";
import {StateContext} from "../context/context";
import {useResource} from "react-request-hook";
import {useNavigate} from "react-router-dom";
import {USER_EVENTS} from "../context/reducer";
import {Button, Container, Form} from "react-bootstrap";


const Login = () => {

    const navigate = useNavigate();
    const {dispatch} = useContext(StateContext);

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loginFailed, setLoginFailed] = useState(false);

    /**
     * mock-server requires email/password in body.
     * query-service requires basic auth header to be passed containing base64(username:password)
     */
    const [loginResp, postLogin] = useResource((username, password) => ({
        url: "/auth/login",
        method: "post",
        headers: { Authorization: `Basic ${btoa(username + ":" + password)}`},
        data: {
            email: username,
            password: password
        }
    }));

    const switchToSignUp = () => {
        navigate("/register");
    }

    useEffect(() => {
        if (loginResp && loginResp.isLoading === false && (loginResp.data || loginResp.error)) {
            if (loginResp.error) {
                setLoginFailed(true);
            } else {
                setLoginFailed(false);
                dispatch({
                    type: USER_EVENTS.LOGIN,
                    payload: {
                        id: loginResp.data?.user?.id || loginResp.data?.id,
                        username: loginResp.data?.user?.username || loginResp.data?.username,
                        token: loginResp.data?.accessToken || loginResp.data?.token
                    }
                });
                navigate("/elections");
            }
        }
    }, [loginResp]);
    

    const handleUsername = event => setUsername(event.target.value);
    const handlePassword = event => setPassword(event.target.value);
    const loginDisabled = !username || username === "" || !password || password === "";

    const handleLogin = event => {
        event.preventDefault();
        postLogin(username, password);
    }

    return (
        <Container className="login_wrapper mt-5 d-flex flex-column justify-content-center">
            <div className="align-self-center w-50 p-5">
                <div className="ps-5 pe-5 pt-3 pb-3 border bg-light">
                    <h2 className="text-center mb-3">Login</h2>
                    <Form onSubmit={handleLogin}>
                        <Form.Group className="mb-3" controlId="login-username">
                            <Form.Label>Username: </Form.Label>
                            <Form.Control placeholder="Enter username" value={username} onChange={handleUsername}/>
                        </Form.Group>
                        <Form.Group className="mb-5" controlId="login-password">
                            <Form.Label>Password: </Form.Label>
                            <Form.Control type="password" placeholder="Enter password" value={password} onChange={handlePassword}/>
                        </Form.Group>
                        <div className="text-center">
                            <Button variant="primary" type="submit" disabled={loginDisabled}>
                                Log in
                            </Button>
                        </div>
                        
                    </Form>
                    {loginFailed && (<span style={{ color: "red" }}>Invalid username or password</span>)}
                </div>     
                <div className="mt-3 ps-5 pe-5 pt-3 pb-3 border text-center bg-light">
                    <span>Don't have an account?</span>
                    <button className="h6 link-button" onClick={switchToSignUp}>Sign up</button>
                </div>
            </div>
        </Container>
    )
}

export default Login