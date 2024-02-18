import { useEffect, useState } from 'react';
import ElectionTile from '../components/ElectionTile';
import axios from 'axios';

const AllElections = () => {
    // const {user} = useAuthContext();
    let user = true;

    const [elections, setElections] = useState([]);

    const fetchData = () => {
        axios.get("http://localhost:8080/api/elections")
        .then((response) => {
        setElections(response.data);
        // console.log(response.status);
        // console.log(response.statusText);
        // console.log(response.headers);
        // console.log(response.config);
        })
        .catch(function(error) {
            console.log(error);
          });
    }

    // this is for filtering. Use later
    // const es = elections.filter((x) => x.electionCategory == 'Politics')
    // console.log(es)


    // fetchData();
    
    useEffect(() => {
        
        if(user) {
            fetchData();
            const refresh = setInterval(() => {
                fetchData();
            }, 10000)
            return () => clearInterval(refresh);
        }
    }, [])

    const testFunction = ()=> {
        // console.log('window.location.href')
        // console.log(window.location.href)
        // console.log('window.location.pathname')
        // console.log(window.location.pathname)
        const candidates = '{"Candidate 1": 2, "Candidate Two": 34}'
        const obj1 = JSON.parse(candidates)
        console.log(obj1);
    }

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