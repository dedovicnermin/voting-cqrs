import { useEffect, useState } from 'react';
import ElectionTile from '../components/ElectionTile';
import axios from 'axios';

const MyElections = () => {

    // const {user} = useAuthContext();
    let user = true;

    const [myElections, setMyElections] = useState([]);

    useEffect(() => {
        setMyElections([]);
    }, [])

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
                    <h3 className="">You have not created any elections yet.</h3>
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
                <div className="row mt-3 mb-3">
                    <button className=" w-50">create election</button>
                </div>
            </div>
        </div>
    );
}
export default MyElections