import { useEffect, useState } from 'react';
import ElectionTile from '../components/ElectionTile';
import axios from 'axios';
import MockData from '../MockData'

const AllElections = () => {
    // const {user} = useAuthContext();
    let user = "Test user";

    const [elections, setElections] = useState([]);

    const fetchData = async () => {
        // try {
        // const response = await axios.get(
        //     `http://localhost:8080/api/elections`
        // );
        // setElections(response.data);
        // }
        // catch (error) {
        //     console.log("Something went wrong");
        //     console.log(error);  
        // }
        setElections(MockData)
    }

    // this is for filtering. Use later
    // const es = elections.filter((x) => x.electionCategory == 'Politics')
    // console.log(es)
    
    useEffect(() => {
        // if(user) {
        //     fetchData();
        //     const refresh = setInterval(() => {
        //         fetchData();
        //     }, 10000)
        //     return () => clearInterval(refresh);
        // }
        setElections(MockData);
    }, [])

    const displayElections = () => {
        if(!user) {
            return (
                <div className="text-center mt-5">
                    <h3 className="">Nothing to see here</h3>
                </div>
            );
        }
        if (elections.length > 0) {
            return (
                <div className="electionsList">
                    <div className="d-flex flex-wrap justify-content-start">
                        {elections.map(
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
                    <h3 className="">There are no active elections at this time.</h3>
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
export default AllElections