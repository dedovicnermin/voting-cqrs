import {useContext, useState} from "react";
import {StateContext} from "../context/context";
import {Container, Col, Row} from "react-bootstrap";
import ElectionCard from "./ElectionCard";
import ElectionCategoryDD from "./dropdown/ElectionCategoryDD";

export default function ElectionList() {

    const {state} = useContext(StateContext)
    const {elections} = state;
    const [category, setCategory] = useState("All")

    const handleOnSelect = (eventKey) => setCategory(eventKey);

    const filterSwitch = c => {
        switch (category) {
            case 'All':
                return true;
            default:
                return c.category === category;
        }
    }

    return (
        <>
            <Container>
                <Row className="mt-3 mb-3">
                    <Col>
                        <ElectionCategoryDD category={category} onSelect={handleOnSelect}/>
                    </Col>
                </Row>
                <div className="d-flex flex-wrap justify-content-start">
                    {
                        elections &&
                        elections.filter(filterSwitch).map((election) => (
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