import {Dropdown} from "react-bootstrap";

export const ELECTION_CATEGORIES = {
    SOCIAL: "Social",
    MUSIC: "Music",
    SPORTS: "Sports",
    POLITICS: "Politics",
    GAMING: "Gaming",
    RANDOM: "Random",
    PYTHON: "python"
}
export default function CategoryDDItems() {
    return (
        <>
            <Dropdown.Item eventKey={ELECTION_CATEGORIES.SOCIAL}>{ELECTION_CATEGORIES.SOCIAL}</Dropdown.Item>
            <Dropdown.Item eventKey={ELECTION_CATEGORIES.MUSIC}>{ELECTION_CATEGORIES.MUSIC}</Dropdown.Item>
            <Dropdown.Item eventKey={ELECTION_CATEGORIES.SPORTS}>{ELECTION_CATEGORIES.SPORTS}</Dropdown.Item>
            <Dropdown.Item eventKey={ELECTION_CATEGORIES.POLITICS}>{ELECTION_CATEGORIES.POLITICS}</Dropdown.Item>
            <Dropdown.Item eventKey={ELECTION_CATEGORIES.GAMING}>{ELECTION_CATEGORIES.GAMING}</Dropdown.Item>
            <Dropdown.Item eventKey={ELECTION_CATEGORIES.RANDOM}>{ELECTION_CATEGORIES.RANDOM}</Dropdown.Item>
            <Dropdown.Item eventKey={ELECTION_CATEGORIES.PYTHON}>{ELECTION_CATEGORIES.PYTHON}</Dropdown.Item>
        </>
    )
}
