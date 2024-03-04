import {useEffect, useState} from "react";
import {useResource} from "react-request-hook";
import {useNavigate} from "react-router-dom";
import {Container, Form, Button} from "react-bootstrap";

const Register = () => {

    const [status, setStatus] = useState("");
    const [formData, setFormData] = useState({
        username: "",
        password: "",
        passwordRepeat: ""
    });

    const [registerResp, postRegister] = useResource((username, password) => ({
        url: "/auth/register",
        method: "post",
        data: {
            email: username,
            username: username,
            password: password
        }
    }))

    const navigate = useNavigate();

    const switchToLogin = () => {
        navigate("/login");
    }

    useEffect(() => {
        if (registerResp && registerResp.isLoading === false && (registerResp.data || registerResp.error)) {
            if (registerResp.error) {
                setStatus(`Registration failed : ${registerResp.error.data.message}`);
            } else {
                setStatus("Registration successful.");
                setTimeout(() => {
                    navigate("/login");
                }, 1000);
            }
        }
    }, [registerResp]);

    const handleSubmit = event => {
        event.preventDefault();
        postRegister(formData.username, formData.password);
    }

    const handleUsername = event => setFormData({...formData, username: event.target.value});
    const handlePassword = event => setFormData({...formData, password: event.target.value});
    const handlePasswordRepeat = event => setFormData({...formData, passwordRepeat: event.target.value});
    const isDisabled= formData.username === "" || formData.password === "" || formData.password !== formData.passwordRepeat;

    return (
        <Container className="login_wrapper mt-5 d-flex flex-column justify-content-center">
            <div className="align-self-center w-50 p-5">
                <div className="ps-5 pe-5 pt-3 pb-3 border bg-light">
                    <h2 className="text-center mb-3">Register</h2>
                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-3" controlId="register-username">
                            <Form.Label>Username: </Form.Label>
                            <Form.Control
                                placeholder="Enter username"
                                type="text"
                                value={formData.username}
                                onChange={handleUsername}
                            />
                        </Form.Group>
                        <Form.Group className="mb-3" controlId="register-password">
                            <Form.Label>Password: </Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Enter password"
                                value={formData.password}
                                onChange={handlePassword}
                            />
                        </Form.Group>
                        <Form.Group className="mb-5" controlId="register-password-repeat">
                            <Form.Label>Repeat Password: </Form.Label>
                            <Form.Control
                                type="password"
                                placeholder="Repeat password"
                                value={formData.passwordRepeat}
                                onChange={handlePasswordRepeat}
                            />
                        </Form.Group>
                        <div className="text-center">
                            <Button variant="primary" type="submit" disabled={isDisabled}>
                                Sign up
                            </Button>
                        </div>
                </Form>
                {status.includes("failed") ? <span style={{color: "red"}}>{status}</span> : <span style={{color: "green"}}>{status}</span>}
                </div>
                <div className="mt-3 ps-5 pe-5 pt-3 pb-3 border text-center bg-light">
                    <span>Have an account?</span>
                    <button className="h6 link-button" onClick={switchToLogin}>Log in</button>
                </div>
            </div>
        </Container>
    );
}

export default Register