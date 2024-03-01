import {Container, Dropdown} from "react-bootstrap";
import CategoryDDItems from "./CategoryDDItems";

export default function ElectionCategoryDD({category, onSelect}) {
    return (
        <Container className="election_filter-category">
            <div className="election_filter-label"><b>Category: </b></div>
            <Dropdown onSelect={onSelect}>
                <Dropdown.Toggle variant="secondary" id="election_category_dropdown">
                    {category}
                </Dropdown.Toggle>
                <Dropdown.Menu>
                    <Dropdown.Item eventKey="All">All</Dropdown.Item>
                    <CategoryDDItems/>
                </Dropdown.Menu>
            </Dropdown>
        </Container>
    )
}
