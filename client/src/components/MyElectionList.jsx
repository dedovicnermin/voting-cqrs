import { useNavigate } from "react-router-dom";
import {useContext, useState} from "react";
import {StateContext} from "../context/context";
// import ElectionCard from "./../component/ElectionCard"
import ElectionCategoryDD from "./../component/dropdown/ElectionCategoryDD";

const MyElections = () => {

    const navigate = useNavigate();
    const navigateToCreateNewElection = () => {
        navigate("/create/");
    }
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
    

    const displayElections = () => {
        // if (myElections.length > 0) {
        //     return (
        //         <div className="electionsList">
        //             <div className="d-flex flex-wrap justify-content-start">
        //                 {myElections.map(
        //                     x =>
        //                     <MyElectionCard/>
        //                 )}
        //             </div>
        //         </div>
        //     )
        // }
        // else {
            return (
                <div className="text-center mt-5">
                    <div className="row mt-3 mb-3">
                        <h3 className="">You have not created any elections yet.</h3>
                    </div>
                    <div className="row mt-3 mb-3">
                        <h6>
                            <span>Try to</span>
                            <button className="h6 link-button" onClick={navigateToCreateNewElection}>create new election</button>
                        </h6>
                    </div>
                </div>
            )
        // }
    }

    return (
        <div>
            <div className="container">
                <div className="row mt-3 mb-3">
                    <div className="col">
                        <ElectionCategoryDD category={category} onSelect={handleOnSelect}/>
                    </div>
                </div>
                {displayElections()}
            </div>
        </div>
    );
}
export default MyElections