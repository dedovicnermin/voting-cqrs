import {Container} from "react-bootstrap";

export default function ElectionHeader({election}) {

    return (
        <Container className="election_header">
            <Container id="election_header-title">
                <div><h3>{election.title}</h3></div>
            </Container>
            <Container className="election_header_meta">
                <Container id="election_header-author">
                    <div><b>Author: </b></div>
                    <div>{election.author}</div>
                </Container>
                <Container id="election_header-category">
                    <div><b>Category: </b></div>
                    <div>{election.category}</div>
                </Container>
                <Container id="election_header-description">
                    <div><b>Description: </b></div>
                    <div>{election.description}</div>
                </Container>
            </Container>

        </Container>
    )

}