import {useContext, useEffect, useState} from "react";
import {StateContext} from "../context/context";
import {Col, Container, Row} from "react-bootstrap";
import ElectionCategoryDD from "./dropdown/ElectionCategoryDD";
import ElectionStatusDD from "./dropdown/ElectionStatusDD";
import ElectionCard from "./ElectionCard";
import {useNavigate} from "react-router-dom";

export default function MyElectionList() {
    const {state} = useContext(StateContext)

    const findMyElections = () => state.elections.filter(e => e.author === state.user.username);
    let elections = findMyElections();
    const navigate = useNavigate();
    const navigateToCreateNewElection = () => {
        navigate("/create/");
    }

    const [category, setCategory] = useState("All")
    const [electionStatus, setElectionStatus] = useState("OPEN");

    const handleOnSelectCategory = (eventKey) => setCategory(eventKey);
    const handleOnSelectStatus = (eventKey) => setElectionStatus(eventKey);

    useEffect(() => {
        elections = findMyElections();
    }, [state.elections]);


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

    const displayElections = () => {
        return (
            <div className="text-center mt-5">
                <div className="row mt-3 mb-3">
                    <h3 className="">You have not created any elections yet.</h3>
                </div>
                <div className="row mt-3 mb-3">
                    <h6>
                        <span>Try to</span>
                        <button className="h6 link-button" onClick={navigateToCreateNewElection}>create new election</button>
                    </h6>
                </div>
            </div>
        )
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
                        elections.length > 0  &&
                        elections.filter(filterCategorySwitch).filter(filterStatusSwitch).map((election) => (
                            <div key={election.id}>
                                <ElectionCard election={election}/>
                            </div>
                        ))
                    }
                </div>
                {
                    elections.length === 0 && displayElections()
                }
            </Container>
        </>
    )
}