import {useContext} from "react";
import {StateContext} from "../context/context";
import {useNavigate} from "react-router-dom";
import {Button, Card, Container} from "react-bootstrap";

export default function ElectionCard({election}) {

    const { state, dispatch } = useContext(StateContext)
    const { user, elections } = state
    const navigate = useNavigate()

    const navigateToElectionDetails = () => {
        navigate(`/elections/${election.id}`, { state: election })
    }

    return (
        <Container onClick={navigateToElectionDetails}>
            <Card style={{ width: '18rem' }}>
                <Card.Body>
                    <Card.Title>{election.title}</Card.Title>
                    <Card.Subtitle>{election.category}</Card.Subtitle>
                    {/*<Card.Footer>*/}
                    {/*    <Button variant="primary">Details</Button>*/}
                    {/*</Card.Footer>*/}
                </Card.Body>
            </Card>
        </Container>
    )




}