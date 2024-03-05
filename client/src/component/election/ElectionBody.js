import {Container, Row, Col, Card, CardTitle, CardSubtitle} from "react-bootstrap";
import {useContext,useEffect} from "react";
import {StateContext} from "../../context/context";

export default function ElectionBody({id}) {

    const { state } = useContext(StateContext);
    const { elections } = state;

    const election = elections.filter(c => c.id === id)[0];

    const updateScore = () => {
        let totalScore = Object.values(election.candidates).reduce((accumulator, currentValue) => {
            return accumulator + currentValue
        }, 0);
        console.log(totalScore)
        let allCandidates = document.getElementsByClassName("score")
        console.log(allCandidates)
        Array.prototype.forEach.call(allCandidates, function(x) {
            // Do stuff here
            console.log(x);
        });
        // let element = e.target
        // let share = (totalScore > 0) ? parseInt(element.textContent) / totalScore * 100 : 0;
        // element.style.width = share + "%"
    }

    useEffect(() => {
        updateScore()
      });

    return (
        <Container className="election_body border pt-1 pb-3">
            <div id="election_body-title" className="text-center">
                <h4>LIVE RESULTS</h4>
            </div>
            <div id="election_body-results" className="d-flex flex-row flex-wrap justify-content-evenly">
                {
                    Object.entries(election.candidates).map(([candidate, score]) => (
                        <Card className="election_body-candidate p-1" key={candidate}>
                            <CardSubtitle className="text-center mt-1 mb-1">{candidate}</CardSubtitle>
                            <div className="text-center border">
                                <div className="score" onChange={updateScore}>{score}</div>
                            </div>
                        </Card>
                    ))
                }
            </div>
        </Container>
    )
}