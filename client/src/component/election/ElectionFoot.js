import {Button, Container, Form} from "react-bootstrap";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";

export default function ElectionFoot({election}) {

    const DEFAULT_SELECTION = "Select candidate";
    const navigate = useNavigate();
    // const eventKey = `${user.id}:${election.id}`;
    const [selectedCandidate, setSelectedCandidate] = useState();

    const handleSelectChange = event => {
        setSelectedCandidate(event.currentTarget.value);
    }
    const handleSubmitVote = event => {
        event.preventDefault()
        console.log("SUBMIT")
        alert(`Vote request for '${selectedCandidate}' successfully sent`)
        navigate("/elections");
    }

    const displayVoteButton = () => selectedCandidate !== undefined &&
        selectedCandidate !== "" &&
        selectedCandidate !== DEFAULT_SELECTION;


    return (
        <Container className="election_foot">
            <Form onSubmit={handleSubmitVote}>
                <Form.Label>Vote for election</Form.Label>
                <Form.Select onChange={handleSelectChange} value={selectedCandidate}>
                    <option key={DEFAULT_SELECTION} value={DEFAULT_SELECTION}>{DEFAULT_SELECTION}</option>
                    {
                        Object.keys(election.candidates).map(candidate =>
                            <option key={candidate} value={candidate}>{candidate}</option>
                        )
                    }
                </Form.Select>
                <Button variant="primary" disabled={displayVoteButton() === false} type="submit">Request Vote</Button>

            </Form>
        </Container>
    )
}