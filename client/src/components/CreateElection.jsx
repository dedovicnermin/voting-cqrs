import {useEffect, useState} from "react";
import { useNavigate } from "react-router-dom";
import {Container, Col, Row, Card, Button, Form} from "react-bootstrap";
import CategoryDDItems from "./../component/dropdown/CategoryDDItems";

const CreateElection = () => {
    const limitMin = 2;
    const limitMax = 8;
    const [category, setCategory] = useState("All")
    const navigate = useNavigate();

    const submit = () => {
        alert("[Created new election] dummy");
        navigate("/elections");
    }

    const deleteCandidate = (e) => {
        e.preventDefault();
        e.target.parentElement.remove();
        let currentSize = document.getElementsByClassName("new-election-candidate p-1").length;
        if (currentSize <= limitMax - 1) {
            const plusButton = document.getElementsByClassName("new-election-candidate-plus")[0];
            console.log(plusButton.getAttribute("class"))
            plusButton.setAttribute("class", plusButton.getAttribute("class").replace(" visually-hidden", ""));
        }
    }

    const plusButton = document.getElementsByClassName("new-election-candidate-plus")[0];

    const addCandidate = () => {
        let currentSize = document.getElementsByClassName("new-election-candidate p-1").length;

        const field = document.getElementById("new-election-all-candidates")

        const newElem = document.createElement("div");
        newElem.setAttribute("class", "new-election-candidate p-1");

        const formControl = document.createElement("input");
        formControl.setAttribute("class", "pt-3 pb-3 form-control");
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

    return (
        <>
            <Container>
                <Row className="mt-3 p-1">
                </Row>
                <Row>
                    <Col>
                        <Card className="election mt-5 p-3 pt-1">
                            <Form>
                                <Row>
                                    <Col className="d-flex justify-content-center">
                                        <Form.Control id="new-election-title" placeholder="Election Title" value={""} className="text-center w-50 h4"/>
                                    </Col>
                                </Row>
                                <Row>
                                    <Col md={8}>
                                        <Form.Group className="mb-1 ps-3" controlId="new-election-description">
                                            <div><b>Description: </b></div>
                                            <Form.Control type="text" placeholder="Add a description" value={""}/>
                                        </Form.Group>
                                    </Col>
                                    <Col md={4}>
                                        <div>
                                            <b>Category: </b>
                                            <Form.Select>
                                                <option>Default select</option>
                                                <option>Default select1</option>
                                                <option>Default select2</option>
                                            </Form.Select>
                                        </div>
                                    </Col>
                                </Row>
                                <Container className="border pt-1 pb-3 mt-0 mb-3 ">
                                    <div id="election_body-title" className="text-center">
                                        <h4>ADD 2 - 8 CANDIDATES</h4>
                                    </div>
                                    <div id="new-election-all-candidates" className="d-flex flex-row flex-wrap justify-content-start ps-5 pe-5">
                                        <div className="new-election-candidate p-1">
                                            <Form.Control type="text" placeholder="New candidate" value={""} className="pt-3 pb-3"/>
                                            <div className="new-election-delete-candidate" onClick={(e) => deleteCandidate(e)}>X</div>
                                        </div>
                                        <div className="new-election-candidate p-1">
                                            <Form.Control type="text" placeholder="New candidate" value={""} className="pt-3 pb-3"/>
                                            <div className="new-election-delete-candidate" onClick={(e) => deleteCandidate(e)}>X</div>
                                        </div>
                                        <div className="new-election-candidate-plus p-1 order-last">
                                            <Button className="h-100 w-100 text-center  pt-3 pb-3" onClick={addCandidate}>
                                                +1
                                            </Button>
                                        </div>
                                    </div>
                                </Container>
                                <div id="election_foot-button" className="text-center d-flex justify-content-center">
                                    <Button variant="primary" disabled={false} type="submit" size="md" className="mt-1 mb-3">Submit</Button>
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