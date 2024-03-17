import {Container, Dropdown} from "react-bootstrap";

export default function ElectionStatusDD({status, onSelect}) {
    return (
        <Container className="election_filter-category">
            <div className="election_filter-label"><b>Status: </b></div>
            <Dropdown onSelect={onSelect}>
                <Dropdown.Toggle variant="secondary" id="election_category_dropdown">
                    {status}
                </Dropdown.Toggle>
                <Dropdown.Menu>
                    <Dropdown.Item eventKey="OPEN">OPEN</Dropdown.Item>
                    <Dropdown.Item eventKey="CLOSED">CLOSED</Dropdown.Item>
                </Dropdown.Menu>
            </Dropdown>
        </Container>
    )
}