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
        let allCandidates = document.getElementsByClassName("score")
        Array.prototype.forEach.call(allCandidates, function(elem) {
            let share = (totalScore > 0) ? parseInt(elem.textContent) / totalScore * 100 : 0
            elem.style.width = share + "%";
            if (share < 5) {
                elem.style.color = "black";
            }
            else {
                elem.style.color = "white";
            }
            
        });
    }

    useEffect(() => {
        updateScore()
      });

    return (
        <Container className="election_body border pt-1 pb-3 mb-1">
            <div id="election_body-title" className="text-center">
                {election.status === 'OPEN' && <h4>LIVE RESULTS</h4> || <h4>RESULTS</h4>}
            </div>
            <div id="election_body-results" className="d-flex flex-row flex-wrap justify-content-evenly">
                {
                    Object.entries(election.candidates).map(([candidate, score]) => (
                        <Card className="election_body-candidate p-1" key={candidate}>
                            <CardSubtitle className="text-center mt-1 mb-1">{candidate}</CardSubtitle>
                            <div className="score-bg text-center border">
                                <div className="score" onChange={updateScore}>{score}</div>
                            </div>
                        </Card>
                    ))
                }
            </div>
        </Container>
    )
}