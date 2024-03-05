import { useNavigate } from "react-router-dom";
import {Container, Col, Row, Card} from "react-bootstrap";

const CreateElection = () => {

    const navigate = useNavigate();

    const submit = () => {
        alert("[Created new election] dummy");
        navigate("/elections");
    }

    const addRow = () => {
        alert("This is not working yet");
    }

    return (
        <>
            <Container>
                <div className="mt-5 mb-3">
                    <form>
                        <div className="mb-3">
                            <label for="title" className="form-label">Title</label>
                            <input type="text" className="form-control" id="title" autoComplete="off"/>
                        </div>
                        <div className="mb-3">
                            <label for="description">Description</label>
                            <textarea className="form-control" placeholder="You can add more details here" id="description" autoComplete="off"></textarea>
                        </div>
                        <div className="mb-3">
                            <label for="category">Category</label>
                            <select className="form-select" id="category" aria-label="Choose a category">
                                <option value="1">Social</option>
                                <option value="2">Music</option>
                                <option value="3">Sports</option>
                                <option value="4">Politics</option>
                                <option value="5">Gaming</option>
                                <option value="6">Whatever...</option>
                            </select>
                        </div>
                        <div className="mb-3">
                            <fieldset>
                                <label>Add from 2 to 8 candidates</label>
                                <input type="text" className="form-control mb-1" id="candidate1" autoComplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate2" autoComplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate3" autoComplete="off"/>
                                <div className="border">
                                    <button  type="reset" className="btn w-100" onClick={addRow}>+ add row</button>
                                </div>
                                
                            </fieldset>
                        </div>
                        <div className="d-flex justify-content-center">
                            <button type="reset" className="btn btn-primary mt-3 mb-5 w-50" onClick={submit}>Create</button>
                        </div>
                    </form>
                </div>
            </Container>
        </>
    );
}

export default CreateElection;