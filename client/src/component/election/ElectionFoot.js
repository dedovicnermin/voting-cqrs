import {Button, Container, Form} from "react-bootstrap";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";

export default function ElectionFoot({election}) {

    const DEFAULT_SELECTION = "Select candidate";
    const navigate = useNavigate();
    // const eventKey = `${user.id}:${election.id}`;
    const [selectedCandidate, setSelectedCandidate] = useState();

    /**
     * Update selectedCandidate state when user selects a value from drop-down
     * @param event containing drop-down current value
     */
    const handleSelectChange = event => {
        setSelectedCandidate(event.currentTarget.value);
    }

    /**
     * Invoked when form button has been clicked
     * @param event formEvent
     */
    const handleSubmitVote = event => {
        event.preventDefault()
        alert(`Vote request for '${selectedCandidate}' successfully sent`)
        navigate("/elections");
    }

    /**
     * Conditional for displaying submit vote button
     * Do not allow vote to be sent if a candidate from election was not selected
     * @returns {boolean}
     */
    const displayVoteButton = () => selectedCandidate !== undefined &&
        selectedCandidate !== "" &&
        selectedCandidate !== DEFAULT_SELECTION;


    return (
        <Container className="election_foot">
            <div id="election_foot-title"><h4>VOTE</h4></div>
            <Form onSubmit={handleSubmitVote}>
                <Form.Group controlId="formSelectCandidate">
                    <Form.Select onChange={handleSelectChange} value={selectedCandidate}>
                        <option key={DEFAULT_SELECTION} value={DEFAULT_SELECTION}>{DEFAULT_SELECTION}</option>
                        {
                            Object.keys(election.candidates).map(candidate =>
                                <option key={candidate} value={candidate}>{candidate}</option>
                            )
                        }
                    </Form.Select>
                    <Form.Text muted>Only one vote will be counted per election</Form.Text>
                </Form.Group>
                <div id="election_foot-button">
                    <Button variant="primary" disabled={displayVoteButton() === false} type="submit" size="md">Submit</Button>
                </div>
            </Form>
        </Container>
    )
}