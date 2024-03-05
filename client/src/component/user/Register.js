import {useEffect, useState} from "react";
import {useResource} from "react-request-hook";
import {Container, Form, Button} from "react-bootstrap";

export default function Register() {

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

    useEffect(() => {
        if (registerResp && registerResp.isLoading === false && (registerResp.data || registerResp.error)) {
            if (registerResp.error) {
                setStatus(`Registration failed : ${registerResp.error.data.message}`);
            } else {
                setStatus("Registration successful. You may now login.");
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
        <Container className="register_wrapper">
            <h2>Register</h2>
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

                <Form.Group className="mb-3" controlId="register-password-repeat">
                    <Form.Label>Repeat Password: </Form.Label>
                    <Form.Control
                        type="password"
                        placeholder="Repeat password"
                        value={formData.passwordRepeat}
                        onChange={handlePasswordRepeat}
                    />
                </Form.Group>

                <Button
                    variant="primary" type="submit"
                    disabled={isDisabled}
                >
                    Register
                </Button>
            </Form>
            {status.includes("failed") ? <span style={{color: "red"}}>{status}</span> : <span style={{color: "green"}}>{status}</span>}
        </Container>
    );

}