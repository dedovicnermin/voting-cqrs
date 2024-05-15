import {Container, Row, Col} from "react-bootstrap";
import {useRSocket} from "../rsocket/RSocketProvider";
import {useEffect} from "react";

export default function ElectionHeader({election}) {

    const {sendFireAndForget} = useRSocket();
    /**
     * This should only run on mount
     */
    useEffect(() => {
        const nowTs = new Date().getTime();
        let status = election.status;
        if (status !== 'CLOSED' && nowTs >= election.endTs) {
            status = 'PENDING'
        }
        sendFireAndForget('new-view', election.id, status, false);
    }, []);

    const print_date = (ts) => {
        const date = new Date(ts);
        let yy = date.getFullYear();
        let mm = date.getMonth() + 1;
        let dd =  date.getDate();
        mm = (mm >= 10) ? mm : "0" + mm;
        dd = (dd >= 10) ? dd : "0" + dd;
        return mm + "/" + dd + "/" + yy + " at " + date.toLocaleTimeString([], {timeStyle: 'short'});
    }

    
    return (
        <Container className="election_header mb-3">
            <Row id="election_header-title">
                <Col className="text-center">
                    <h3>{election.title}</h3>
                </Col>
            </Row>
            <Row>
                <Col md={8}>
                    <div><b>Description: </b></div>
                    <div className="overflow-y-auto h-50">{election.description}</div>
                </Col>
                <Col md={4}>
                    <div>
                        <b>Category: </b>
                        <span>{election.category}</span>
                    </div>
                    <div>
                        <b>Author: </b>
                        <span>{election.author}</span>
                    </div>
                    <div>
                        <b>Begin: </b>
                        <span>{print_date(election.startTs)}</span>
                    </div>
                    <div>
                        <b>End: </b>
                        <span>{print_date(election.endTs)}</span>
                    </div>
                    <div>
                        <b>Status: </b>
                        <span>{election.status}</span>
                    </div>
                </Col>
            </Row>
        </Container>
    )
}