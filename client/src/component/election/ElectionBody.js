import {Container} from "react-bootstrap";

export default function ElectionBody({election}) {


    return (
        <Container className="election_body">
            {
                Object.entries(election.candidates).map(([candidate, score]) => (
                    <Container className="election_body-candidate">
                        <div>{candidate}</div>
                        <div>{score}</div>
                    </Container>
                ))
            }
        </Container>
    )
}