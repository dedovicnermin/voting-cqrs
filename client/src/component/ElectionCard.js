import {useNavigate} from "react-router-dom";
import {Card} from "react-bootstrap";

export default function ElectionCard({election}) {

    const navigate = useNavigate()

    const navigateToElectionDetails = () => {
        navigate(`/elections/${election.id}`, { state: election })
    }

    return (
        <Card style={{ width: '25rem'}} onClick={navigateToElectionDetails}>
            <Card.Body>
                <Card.Title className="text-truncate">{election.title}</Card.Title>
                <Card.Text>{election.category}</Card.Text>
            </Card.Body>
        </Card>
    )
}