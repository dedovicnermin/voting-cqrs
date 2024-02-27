import {useContext, useState} from "react";
import {StateContext} from "../context/context";
import {Container} from "react-bootstrap";
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
            <Container className="mt-4">
                <ElectionCategoryDD category={category} onSelect={handleOnSelect}/>
            </Container>
            <Container className="election_list">
                {
                    elections &&
                    elections.filter(filterSwitch).map((election) => (
                        <Container key={election.id}>
                            <ElectionCard election={election}/>
                        </Container>
                    ))
                }
            </Container>
        </>
    )

}