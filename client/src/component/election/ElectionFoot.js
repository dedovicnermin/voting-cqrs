import {Button, Container, Form} from "react-bootstrap";
import {useState, useContext, useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {StateContext} from "../../context/context";
import {useResource} from "react-request-hook";
import client, {connectRSocket} from "../rsocket/CommandClient";
import {
    encodeRoute,
    encodeAndAddCustomMetadata,
    encodeCompositeMetadata,
    WellKnownMimeType,
    BufferEncoder
} from "rsocket-core";
import {encodeCustomMetadataHeader} from "rsocket-core/build/CompositeMetadata";
import {fireElectionVote} from "../rsocket/rsHelper";

export default function ElectionFoot({election}) {

    const DEFAULT_SELECTION = "Select candidate";
    const navigate = useNavigate();
    const [selectedCandidate, setSelectedCandidate] = useState();
    const { user } = useContext(StateContext).state
    const eventKey = `${user.id}:${election.id}`;


    // const [eventResp, sendEvent] = useResource((voteEvent) => ({
    //     url: process.env.REACT_APP_VOTE_ENDPOINT,
    //     method: "post",
    //     data: {
    //         key: {
    //             type: "STRING",
    //             data: eventKey
    //         },
    //         value: {
    //             type: "JSON",
    //             data: voteEvent
    //         }
    //     },
    //     headers: { Authorization: `Basic ${btoa('nermin' + ':' + 'nermin-secret')}`}
    // }));

    /**
     * Update selectedCandidate state when user selects a value from drop-down
     * @param event containing drop-down current value
     */
    const handleSelectChange = event => {
        setSelectedCandidate(event.currentTarget.value);
    }

    /**
     * Invoked when form button has been clicked
     * @param event formEvent
     */
    const handleSubmitVote = event => {
        event.preventDefault();
        fireElectionVote(
            eventKey,
            {
                electionId: election.id,
                votedFor: selectedCandidate
            }
        ).then(r => console.log("ElectionFoot -> .then() I AM HERE"));
        // let socket;
        // const connectAndSend = async () => {
        //     try {
        //         socket = await client.connect()
        //         console.log('Connected to CMD RSocket server');
        //         const customMimeType = 'messaging/key';
        //         console.log('NERM: ' + eventKey);
        //         const customMetadataEntry = {
        //             mimeType: customMimeType,
        //             data: Buffer.from(eventKey)
        //         };
        //
        //         // const customMetadata = encodeAndAddCustomMetadata('message/key', new TextEncoder().encode(eventKey));
        //         const metadata = encodeCompositeMetadata([
        //             [WellKnownMimeType.MESSAGE_RSOCKET_ROUTING, encodeRoute('new-vote')],
        //             // ['message/key', customMetadata]
        //             [customMimeType, BufferEncoder.encode(customMetadataEntry.data)]
        //         ]);
        //
        //         socket.fireAndForget({
        //             data: {
        //                 electionId: election.id,
        //                 votedFor: selectedCandidate
        //             },
        //             metadata: metadata
        //         });
        //
        //         socket.close();
        //     } catch (error) {
        //         console.error('Connection failed or message sending error: ', error)
        //     }
        // };
        //
        // connectAndSend()
        // if (socket) {
        //     socket.close();
        // }

        // client.connect().subscribe({
        //     onComplete: socket => {
        //         console.log('Connected to CMD RSocket server');
        //         const customMimeType = 'messaging/key';
        //         const customMetadataEntry = {
        //             mimeType: customMimeType,
        //             data: Buffer.from(eventKey)
        //         };

                // const customMetadata = encodeAndAddCustomMetadata('message/key', new TextEncoder().encode(eventKey));
                // const metadata = encodeCompositeMetadata([
                //     [WellKnownMimeType.MESSAGE_RSOCKET_ROUTING, encodeRoute('new-vote')],
                //     ['message/key', customMetadata]
                    // [customMimeType, BufferEncoder.encode(customMetadataEntry.data)]
                // ]);

                // socket.fireAndForget({
                //     data: {
                //         electionId: election.id,
                //         votedFor: selectedCandidate
                //     },
                //     metadata: metadata
                // });
            // },
            // onError: error => console.error("Connection has failed", error),
            // onClose: socket => socket.close()
        // });
        // sendEvent({
        //     electionId: election.id,
        //     votedFor: selectedCandidate
        // });
        alert(`Vote request for '${selectedCandidate}' successfully sent`)
        navigate("/elections");
    }

    /**
     * Conditional for displaying submit vote button
     * Do not allow vote to be sent if a candidate from election was not selected
     * @returns {boolean}
     */
    const displayVoteButton = () => selectedCandidate !== undefined &&
        selectedCandidate !== "" &&
        selectedCandidate !== DEFAULT_SELECTION;


    return (
        <Container className="election_foot border">
            <div className="text-center">
                <h4>VOTE</h4>
            </div>
            <Form onSubmit={handleSubmitVote}>
                <Form.Group controlId="formSelectCandidate">
                    <div className="d-flex justify-content-center">
                        <Form.Select className="w-50 " onChange={handleSelectChange} value={selectedCandidate}>
                            <option key={DEFAULT_SELECTION} value={DEFAULT_SELECTION}>{DEFAULT_SELECTION}</option>
                            {
                                Object.keys(election.candidates).map(candidate =>
                                    <option key={candidate} value={candidate}>{candidate}</option>
                                )
                            }
                        </Form.Select> 
                    </div>
                    <div className="d-flex justify-content-center">
                        <Form.Text muted>Only one vote will be counted per election</Form.Text>
                    </div>
                </Form.Group>
                <div id="election_foot-button" className="text-center d-flex justify-content-center">
                    <Button variant="primary" disabled={displayVoteButton() === false} type="submit" size="md" className="mt-1 mb-3">Submit</Button>
                </div>
            </Form>
        </Container>
    )
}