import { useEffect, useState } from 'react';
import ElectionTile from '../components/ElectionTile';
import { useNavigate } from "react-router-dom";
import axios from 'axios';

const MyElections = () => {

    // const {user} = useAuthContext();
    let user = true;

    const navigate = useNavigate();

    const [myElections, setMyElections] = useState([]);

    const fetchData = async () => {
        try {
        const response = await axios.get(
            `http://localhost:8080/api/users/1/elections`
        );
        setMyElections(response.data);
    }
    catch (error) {
        console.log("Something went wrong");
        console.log(error);  
        }
    }

    useEffect(() => {
        if(user) {
            fetchData();
            const refresh = setInterval(() => {
                fetchData();
            }, 10000)
            return () => clearInterval(refresh);
        }
    }, [])

    const navigateToCreateNewElection = () => {
        navigate("/create/");
    }

    const displayElections = () => {
        if(!user) {
            return (
                <div className="text-center mt-5">
                    <h3 className="">Nothing to see here</h3>
                </div>
            );
        }
        if (myElections.length > 0) {
            return (
                <div className="electionsList">
                    <div className="d-flex flex-wrap justify-content-start">
                        {myElections.map(
                            x =>
                            <ElectionTile key={x.id} id={x.id} title={x.title} category={x.category}/>
                        )}
                    </div>
                </div>
            )
        }
        else {
            return (
                <div className="text-center mt-5">
                    <div className="row mt-3 mb-3">
                        <h3 className="">You have not created any elections yet.</h3>
                    </div>
                    <div className="row mt-3 mb-3">
                        <h6>
                            <span>Try to</span>
                            <button className="h6 create-new-btn" onClick={navigateToCreateNewElection}>create new election</button>
                        </h6>
                    </div>
                </div>
            )
        }
    }

    return (
        <div>
            <div className="container">
                <div className="row mt-3 mb-3">
                    <div className="col">
                    </div>
                    <div className="col text-end">
                        filter will be here: 
                        <select>
                            <option>Category 1</option>
                            <option>Category 2</option>
                            <option>Category 3</option>
                        </select>
                    </div>
                </div>
                {displayElections()}
                
            </div>
        </div>
    );
}
export default MyElections