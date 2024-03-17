import {useContext, useState} from "react";
import {StateContext} from "../context/context";
import {Container, Col, Row} from "react-bootstrap";
import ElectionCard from "./ElectionCard";
import ElectionCategoryDD from "./dropdown/ElectionCategoryDD";
import ElectionStatusDD from "./dropdown/ElectionStatusDD";

export default function ElectionList() {

    const {state} = useContext(StateContext)
    const {elections} = state;
    const [category, setCategory] = useState("All")
    const [electionStatus, setElectionStatus] = useState("OPEN");

    const handleOnSelectCategory = (eventKey) => setCategory(eventKey);
    const handleOnSelectStatus = (eventKey) => setElectionStatus(eventKey);


    const filterCategorySwitch = c => {
        switch (category) {
            case 'All':
                return true;
            default:
                return c.category === category;
        }
    }

    const filterStatusSwitch = election => {
        switch (electionStatus) {
            case "OPEN":
                return election.status === "OPEN";
            default:
                return election.status === "CLOSED";
        }
    }

    return (
        <>
            <Container>
                <Row className="mt-1 mb-1">
                    <Col>
                        <ElectionCategoryDD category={category} onSelect={handleOnSelectCategory}/>
                        <ElectionStatusDD status={electionStatus} onSelect={handleOnSelectStatus}/>
                    </Col>
                </Row>
                <div className="d-flex flex-wrap justify-content-start">
                    {
                        elections &&
                        elections.filter(filterCategorySwitch).filter(filterStatusSwitch).map((election) => (
                            <div key={election.id}>
                                <ElectionCard election={election}/>
                            </div>
                        ))
                    }
                </div>
            </Container>
        </>
    )
}