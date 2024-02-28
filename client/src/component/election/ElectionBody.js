import {Container} from "react-bootstrap";
import {useContext} from "react";
import {StateContext} from "../../context/context";

export default function ElectionBody({id}) {

    const { state } = useContext(StateContext);
    const { elections } = state;

    const election = elections.filter(c => c.id === id)[0];

    return (
        <Container className="election_body">
            <div id="election_body-title"><h4>LIVE RESULTS</h4></div>
            <div id="election_body-results">
                {
                    Object.entries(election.candidates).map(([candidate, score]) => (
                        <Container className="election_body-candidate" key={candidate}>
                            <div>{candidate}</div>
                            <div>{score}</div>
                        </Container>
                    ))
                }
            </div>
        </Container>
    )
}