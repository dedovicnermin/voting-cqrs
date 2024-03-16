import {useLocation, useNavigate} from "react-router-dom";
import {Container, Col, Row, Card} from "react-bootstrap";
import ElectionHeader from "./ElectionHeader";
import ElectionBody from "./ElectionBody";
import ElectionFoot from "./ElectionFoot";

export default function Election() {

    const navigate = useNavigate();
    let election = useLocation().state;

    const handleBackButtonClick = () => {
        navigate("/elections")
    }

    return (
        <>
            <Container>
                <Row  className="mt-1 mb-1">
                    <Col>
                        <button className="btn btn-secondary" onClick={handleBackButtonClick}>&lt; Back</button>
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <Card className="election p-3">
                            <ElectionHeader election={election}/>
                            <ElectionBody id={election.id}/>
                            <ElectionFoot election={election}/>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </>
    )
}