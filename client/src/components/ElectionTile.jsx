import { useNavigate } from "react-router-dom";

const ElectionListItem = (props) => {
    const id = props.id;
    const title = props.title;
    const category = props.category;

    const navigate = useNavigate();

    const openElectionDetails = () => {
        navigate("/all-elections/" + id);
    }

    return (
        <div className="card" style={{width: "18rem"}} onClick={openElectionDetails}>
            <div className="card-body">
                <h5 className="card-title text-truncate">{title}</h5>
                <p className="card-text">{category}</p>
            </div>
        </div>
    );
}
export default ElectionListItem