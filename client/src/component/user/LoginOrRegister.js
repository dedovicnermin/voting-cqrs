import {Container} from "react-bootstrap";
import Register from "./Register";
import Login from "./Login";

export default function LoginOrRegister() {
    return (
        <Container className="login_or_register_wrapper">
            <Login />
            <Register />
        </Container>
    )
}