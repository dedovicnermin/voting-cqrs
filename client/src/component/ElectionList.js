import {useContext} from "react";
import {StateContext} from "../context/context";
import {Button, Card, Container} from "react-bootstrap";
import ElectionTile from "../components/ElectionTile";
import {useNavigate} from "react-router-dom";
import ElectionCard from "./ElectionCard";

export default function ElectionList() {

    const {state, dispatch} = useContext(StateContext)
    const navigate = useNavigate()

    const {user, elections} = state;

    const renderElection = (election) => {
        navigate("/elections/" + election.id, {state: election})
    }

    return (
        <Container>
            {
                elections?.length > 0 && elections.map((election) => (
                    <Container key={election.id}>
                        {/*<ElectionTile key={election.id} id={election.id} title={election.title} category={election.category} />*/}
                        {/*<Card style={{ width: '18rem'}}>*/}
                        {/*    <Card.Body>*/}
                        {/*        <Card.Title>{election.title}</Card.Title>*/}
                        {/*        <Card.Subtitle>{election.category}</Card.Subtitle>*/}
                        {/*    </Card.Body>*/}
                        {/*</Card>*/}
                        <ElectionCard election={election}/>
                    </Container>
                ))
            }
        </Container>
    )


}