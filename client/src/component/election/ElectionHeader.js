import {Container, Row, Col} from "react-bootstrap";

export default function ElectionHeader({election}) {

    return (
        // <Container className="election_header">
        //     <Container id="election_header-title">
        //         <div><h3>{election.title}</h3></div>
        //     </Container>
        //     <Container className="election_header_meta">
        //         <Container id="election_header-author">
        //             <div><b>Author: </b></div>
        //             <div>{election.author}</div>
        //         </Container>
        //         <Container id="election_header-category">
        //             <div><b>Category: </b></div>
        //             <div>{election.category}</div>
        //         </Container>
        //         <Container id="election_header-description">
        //             <div><b>Description: </b></div>
        //             <div>{election.description}</div>
        //         </Container>
        //     </Container>
        // </Container>
        <Container className="election_header">
            <Row id="election_header-title">
                <Col className="text-center">
                    <h3>{election.title}</h3>
                </Col>
            </Row>
            <Row className="election_header_meta">
                <Col id="election_header-author">
                    
                </Col>
            </Row>
            <Row>
                <Col className="text-center">
                    <div>
                        <b>Author: </b>
                        <span>{election.author}</span>
                    </div>
                </Col>
                <Col className="text-center">
                    <div>
                        <b>Category: </b>
                        <span>{election.category}</span>
                    </div>
                </Col>
            </Row>
            <Row id="election_header-category">
            </Row>
            <Row id="election_header-description">
                <Col>
                <div><b>Description: </b></div>
                </Col>
            </Row>
            <Row id="election_header-description">
                <Col>
                <div>{election.description}</div>
                </Col>
            </Row>
        </Container>
    )

}