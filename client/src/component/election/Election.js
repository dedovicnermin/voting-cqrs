import {useLocation, useNavigate, useParams} from "react-router-dom";
import {useContext, useEffect} from "react";
import {StateContext} from "../../context/context";
import {Container, Form} from "react-bootstrap";
import ElectionHeader from "./ElectionHeader";
import ElectionBody from "./ElectionBody";
import ElectionFoot from "./ElectionFoot";

export default function Election() {

    const {state, dispatch} = useContext(StateContext);
    const {user, elections} = state;
    const navigate = useNavigate();
    const {id} = useParams();

    let election = useLocation().state;

    const refreshElection = () => {
        election = elections.filter((e) => e.id === id)[0]
    }

    /**
     * This will run each time App.js fetches elections
     */
    useEffect(() => {
        refreshElection()
    }, [elections]);

    const handleBackButtonClick = () => {
        navigate("/elections")
    }

    return (
        <>
            <Container className="mt-4 ml-2">
                <Container>
                    <button className="btn btn-secondary" onClick={handleBackButtonClick}>&lt; Back</button>
                </Container>
            </Container>
            <Container className="e-wrapper">
                <Container className="election">
                    <ElectionHeader election={election}/>
                    <ElectionBody election={election}/>
                    <ElectionFoot election={election}/>
                </Container>
            </Container>
        </>
    )
}