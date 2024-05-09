import client from "./CommandClient";
import {encodeCompositeMetadata, encodeRoute, MESSAGE_RSOCKET_ROUTING} from "rsocket-core";

const rsocket = client.connect();
export async function fireElectionVote(key, value) {
    console.log("About to connect to rsocket client, connect, and execute FNF")
    console.log(`FNF (K,V): ${key} --- ${JSON.stringify(value)}` );
    // rsocket.subscribe({
    //     onComplete: socket => {
    //         socket.fireAndForget({
    //             data: Buffer.from(JSON.stringify(value)),
    //             metadata: encodeCompositeMetadata([
    //                 [MESSAGE_RSOCKET_ROUTING, encodeRoute('new-vote')],
    //                 ['messaging/key', Buffer.from(key)]
    //             ])
    //         })
    //         // socket.close();
    //     },
    //     onSubscribe: subscription => subscription,
    //     onError: error => console.log("ERROR: " + error),
    // });

    rsocket.then(
        socket => {
            socket.connectionStatus().subscribe(event => console.log(event))
            socket.fireAndForget({
                data: Buffer.from(JSON.stringify(value)),
                metadata: encodeCompositeMetadata([
                    [MESSAGE_RSOCKET_ROUTING, encodeRoute('new-vote')],
                    ['messaging/key', Buffer.from(key)]
                ])
            })
        },
        error => {
            console.log('error:', error)
        }
    );
}

export async function closeClient() {
    await client.close();
}