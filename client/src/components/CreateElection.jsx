import { useState } from 'react';
import { useNavigate } from "react-router-dom";

const CreateElection = () => {
    // const {user} = useAuthContext();
    const user = "test user";

    const navigate = useNavigate();

    // const [serialNumber, setSerialNumber] = useState();
    // const [buildNumber, setBuildNumber] = useState();
    // const [naviVersion, setNaviVersion] = useState();
    // const [mapVersion, setMapVersion] = useState();
    // const [notes, setNotes] = useState();

    // const savenewElection = async () => {
    //             fetch('https://mywebsite.example/endpoint/', {
    //     method: 'POST',
    //     headers: {
    //         'Accept': 'application/json',
    //         'Content-Type': 'application/json',
    //     },
    //     body: JSON.stringify({
    //         firstParam: 'yourValue',
    //         secondParam: 'yourOtherValue',
    //     })
    //     })
    //     const newElection = {
    //         id: serialNumber,
    //         title: buildNumber,
    //         description: naviVersion,
    //         category: mapVersion,
    //         candidates: notes
    //     }
        
    //     addUnit(newDevice);
    //     hideForm();
    // }

    const submit = () => {
        alert("[Created new election] dummy");
        navigate("/all-elections/");
    }

    const displayForm = () => {
        if(user) {
            return (
                <div className="mt-5 mb-3">
                    <form>
                        <div className="mb-3">
                            <label for="title" className="form-label">Title</label>
                            <input type="text" className="form-control" id="title" autocomplete="off"/>
                        </div>
                        <div className="mb-3">
                            <label for="description">Description</label>
                            <textarea className="form-control" placeholder="You can add more details here" id="description" autocomplete="off"></textarea>
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
                                <label>Add from 2 to 10 candidates</label>
                                <input type="text" className="form-control mb-1" id="candidate1" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate2" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate3" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate4" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate5" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate6" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate7" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate8" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate9" autocomplete="off"/>
                                <input type="text" className="form-control mb-1" id="candidate10" autocomplete="off"/>
                            </fieldset>
                        </div>
                        <div className="d-flex justify-content-center">
                            <button type="reset" className="btn btn-warning mt-3 mb-5 w-50" onClick={submit}>Create</button>
                        </div>
                    </form>
                </div>
            )
        }
        else {
            return (
                <div className="text-center mt-5">
                    <h3 className="">Nothing to see here</h3>
                </div>
            );
        }
    }

    return (
        <div>
            <div className="container">
                {displayForm()}
            </div>
        </div>
    );
}

export default CreateElection;