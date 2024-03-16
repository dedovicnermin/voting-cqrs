import {useState, useContext} from "react";
import {StateContext} from "../context/context";
import { useNavigate } from "react-router-dom";
import {Container, Col, Row, Card, Button, Form} from "react-bootstrap";
import {useResource} from "react-request-hook";

const { ELECTION_CATEGORIES } = require("./../component/dropdown/CategoryDDItems.js");

const CreateElection = () => {

    const DEFAULT_SELECTION = "Select category";

    const {state} = useContext(StateContext)

    const limitMin = 2;
    const limitMax = 8;
    const navigate = useNavigate();

    const [eventResp, sendEvent] = useResource((eventValue) => ({
        url: process.env.REACT_APP_ELECTION_ENDPOINT,
        method: "post",
        data: {
            key: {
                type: "STRING",
                data: state.user.id
            },
            value: {
                type: "JSON",
                data: eventValue
            }
        },
        headers: { Authorization: `Basic ${btoa('nermin' + ':' + 'nermin-secret')}`}
    }));

    const deleteCandidate = (e) => {
        e.preventDefault();
        e.target.parentElement.remove();
        let currentSize = document.getElementsByClassName("new-election-candidate p-1").length;
        if (currentSize <= limitMax - 1) {
            const plusButton = document.getElementsByClassName("new-election-candidate-plus")[0];
            plusButton.setAttribute("class", plusButton.getAttribute("class").replace(" visually-hidden", ""));
        }
        if (currentSize <= limitMin) {
            const deleteButtons = document.getElementsByClassName("new-election-delete-candidate");
            for (let i = 0; i < deleteButtons.length; i++) {
                deleteButtons[i].remove();
            }
        }
    }

    const plusButton = document.getElementsByClassName("new-election-candidate-plus")[0];

    const addCandidate = () => {
        let currentSize = document.getElementsByClassName("new-election-candidate p-1").length;

        const field = document.getElementById("new-election-all-candidates")

        const newElem = document.createElement("div");
        newElem.setAttribute("class", "new-election-candidate p-1");

        const formControl = document.createElement("input");
        formControl.setAttribute("class", "pt-3 pb-3 form-control candidate-value");
        formControl.setAttribute("value", "");
        formControl.setAttribute("type", "text");
        formControl.setAttribute("placeholder", "New candidate");

        const delIcon = document.createElement("div");
        delIcon.setAttribute("class", "new-election-delete-candidate");
        delIcon.addEventListener("click", e => deleteCandidate(e));
        delIcon.innerHTML = "X"
        
        newElem.appendChild(formControl);
        newElem.appendChild(delIcon);
        field.appendChild(newElem);

        if (currentSize >= limitMax - 1) {
            const plusButton = document.getElementsByClassName("new-election-candidate-plus")[0];
            plusButton.setAttribute("class", plusButton.getAttribute("class") + " visually-hidden")
        }
    }

    
    const [formData, setFormData] = useState({
        title: "",
        author: state.user.username,
        description: "",
        category: DEFAULT_SELECTION,
        candidates: []
    });

    const handleTitle = event => setFormData({...formData, title: event.target.value});
    const handleDescription = event => setFormData({...formData, description: event.target.value});
    const handleCategory = event => setFormData({...formData, category: event.currentTarget.value});

    const handleSubmit = (event) => {
        event.preventDefault();
        let candidates = document.getElementsByClassName("candidate-value");
        const candidateArray = []
        for (let i = 0; i < candidates.length; i++) {
            candidateArray.push(candidates[i].value)
        }
        sendEvent({
            title: formData.title,
            author: formData.author,
            description: formData.description,
            category: formData.category,
            candidates: candidateArray
        });
        alert(`Create election request has been sent.`);
        navigate("/elections");
    }

    /**
     * Return true if valid / passes test
     */
    const validateTitle = title => {
        return title !== undefined && title !== "";
    }

    const validateCategory = category => {
        return category !== undefined && category !== DEFAULT_SELECTION;
    }

    const validateCandidates = entries => {
        return Array.from(entries).filter(entry => {
            return (entry.value === undefined || entry.value === "")
        }).length === 0;
    }

    const allowSubmitClick = () => {
        const candidateEntries = document.getElementsByClassName("candidate-value");
        const titleIsGood = validateTitle(formData.title);
        const categoryIsGood = validateCategory(formData.category);
        const candidatesAreGood = validateCandidates(candidateEntries);
        return titleIsGood && categoryIsGood && candidatesAreGood;
    }

    return (
        <>
            <Container>
                <Row className="mt-1 mb-3 p-1">
                </Row>
                <Row>
                    <Col>
                        <Card className="election mt-3 p-3 pt-1">
                            <Form onSubmit={handleSubmit}>
                                <Row>
                                    <Col className="d-flex justify-content-center">
                                        <Form.Control id="new-election-title" className="text-center w-50 h4"
                                            placeholder="Election Title" 
                                            value={formData.title}
                                            onChange={handleTitle}
                                        />
                                    </Col>
                                </Row>
                                <Row>
                                    <Col md={8}>
                                        <Form.Group className="mb-3 ps-3" controlId="new-election-description">
                                            <div><b>Description: </b></div>
                                            <Form.Control type="text" placeholder="Add a description" 
                                                value={formData.description}
                                                onChange={handleDescription}
                                            />
                                        </Form.Group>
                                    </Col>
                                    <Col md={4}>
                                        <div>
                                            <b>Category: </b>
                                            <Form.Select onChange={handleCategory}>
                                                <option key={DEFAULT_SELECTION} value={DEFAULT_SELECTION}>{DEFAULT_SELECTION}</option>
                                                {
                                                    Object.values(ELECTION_CATEGORIES).map(x =>
                                                        <option key={x} value={x}>{x}</option>
                                                    )
                                                }                     
                                            </Form.Select>
                                        </div>
                                    </Col>
                                </Row>
                                <Container className="border pt-1 pb-3 mt-0 mb-3 ">
                                    <div id="election_body-title" className="text-center">
                                        <h4>ADD UP TO 8 CANDIDATES</h4>
                                    </div>
                                    <div id="new-election-all-candidates" className="d-flex flex-row flex-wrap justify-content-start ps-5 pe-5">
                                        <div className="new-election-candidate p-1">
                                            <Form.Control type="text" placeholder="New candidate" className="pt-3 pb-3 candidate-value"/>
                                        </div>
                                        <div className="new-election-candidate p-1">
                                            <Form.Control type="text" placeholder="New candidate" className="pt-3 pb-3 candidate-value"/>
                                        </div>
                                        <div className="new-election-candidate-plus p-1 order-last">
                                            <Button variant="outline-success" className="h-100 w-100 text-center  pt-3 pb-3" onClick={addCandidate}>
                                                +1
                                            </Button>
                                        </div>
                                    </div>
                                </Container>
                                <div id="election_foot-button" className="text-center d-flex justify-content-center">
                                    <Button variant="primary" disabled={allowSubmitClick() === false} type="submit" size="md" className="mt-1 mb-3">Submit</Button>
                                </div>
                            </Form>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </>
    );
}

export default CreateElection;