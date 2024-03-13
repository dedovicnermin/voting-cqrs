import {Container, Row, Col} from "react-bootstrap";

export default function ElectionHeader({election}) {

    return (
        <Container className="election_header">
            <Row id="election_header-title">
                <Col className="text-center">
                    <h3>{election.title}</h3>
                </Col>
            </Row>
            <Row>
                <Col md={8}>
                    <div><b>Description: </b></div>
                    <div className="overflow-y-auto h-50">{election.description}</div>
                </Col>
                <Col md={4}>
                    <div>
                        <b>Category: </b>
                        <span>{election.category}</span>
                    </div>
                    <div>
                        <b>Author: </b>
                        <span>{election.author}</span>
                    </div>
                </Col>
            </Row>
        </Container>
    )

}