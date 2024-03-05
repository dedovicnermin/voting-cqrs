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
            {/* <Container className="mt-4 ml-2">
                <Container>
                    <button className="btn btn-secondary" onClick={handleBackButtonClick}>&lt; Back</button>
                </Container>
            </Container>
            <Container className="e-wrapper">
                <Container className="election">
                    <ElectionHeader election={election}/>
                    <ElectionBody id={election.id}/>
                    <ElectionFoot election={election}/>
                </Container>
            </Container> */}
            
            <Container>
                <Row  className="mt-3 mb-3">
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