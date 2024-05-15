import {Button, Container, Form} from "react-bootstrap";
import {useState, useContext} from "react";
import {useNavigate} from "react-router-dom";
import {StateContext} from "../../context/context";
import { useRSocket } from "../rsocket/RSocketProvider";

export default function ElectionFoot({election}) {

    const DEFAULT_SELECTION = "Select candidate";
    const navigate = useNavigate();
    const [selectedCandidate, setSelectedCandidate] = useState();
    const { user } = useContext(StateContext).state
    const eventKey = `${user.id}:${election.id}`;
    const {sendFireAndForget} = useRSocket();

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
    const handleSubmitVote = async event => {
        event.preventDefault();
        sendFireAndForget('new-vote', eventKey, {electionId: election.id, votedFor: selectedCandidate});
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
        <Container className="election_foot border">
            <div className="text-center">
                <h4>VOTE</h4>
            </div>
            <Form onSubmit={handleSubmitVote}>
                <Form.Group controlId="formSelectCandidate">
                    <div className="d-flex justify-content-center">
                        <Form.Select className="w-50 " onChange={handleSelectChange} value={selectedCandidate}>
                            <option key={DEFAULT_SELECTION} value={DEFAULT_SELECTION}>{DEFAULT_SELECTION}</option>
                            {
                                Object.keys(election.candidates).map(candidate =>
                                    <option key={candidate} value={candidate}>{candidate}</option>
                                )
                            }
                        </Form.Select> 
                    </div>
                    <div className="d-flex justify-content-center">
                        <Form.Text muted>Only one vote will be counted per election</Form.Text>
                    </div>
                </Form.Group>
                <div id="election_foot-button" className="text-center d-flex justify-content-center">
                    <Button variant="primary" disabled={displayVoteButton() === false} type="submit" size="md" className="mt-1 mb-3">Submit</Button>
                </div>
            </Form>
        </Container>
    )
}