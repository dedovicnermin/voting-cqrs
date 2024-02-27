import {useContext} from "react";
import {StateContext} from "../context/context";
import {Container} from "react-bootstrap";
import ElectionCard from "./ElectionCard";

export default function ElectionList() {

    const {state, dispatch} = useContext(StateContext)
    const {elections} = state;

    return (
        <Container className="election_list">
            {
                elections?.length > 0 && elections.map((election) => (
                    <Container key={election.id}>
                        <ElectionCard election={election}/>
                    </Container>
                ))
            }
        </Container>
    )

}