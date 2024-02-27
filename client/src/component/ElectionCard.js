import {useNavigate} from "react-router-dom";
import {Card, Container} from "react-bootstrap";

export default function ElectionCard({election}) {

    const navigate = useNavigate()

    const navigateToElectionDetails = () => {
        navigate(`/elections/${election.id}`, { state: election })
    }

    return (
        <Container onClick={navigateToElectionDetails}>
            <Card style={{ width: '25rem', height: '5rem' }}>
                <Card.Body>
                    <Card.Title>{election.title}</Card.Title>
                    <Card.Subtitle>{election.category}</Card.Subtitle>
                </Card.Body>
            </Card>
        </Container>
    )

}