import client from "./CommandClient";
import {encodeCompositeMetadata, encodeRoute, MESSAGE_RSOCKET_ROUTING} from "rsocket-core";

export async function fireElectionVote(key, value) {
    console.log("About to connect to rsocket client, connect, and execute FNF")
    console.log(`FNF (K,V): ${key} --- ${JSON.stringify(value)}` );
    client.connect().then(
        socket => {
            socket.connectionStatus().subscribe(event => console.log(event));
            socket.fireAndForget({
                data: Buffer.from(JSON.stringify(value)),
                metadata: encodeCompositeMetadata([
                    [MESSAGE_RSOCKET_ROUTING, encodeRoute('new-vote')],
                    ['messaging/key', Buffer.from(key)]
                ])
            });

            // .subscribe({
            //     onComplete: () => console.log('Fire vote command completed'),
            //     onError: error => console.error("Fire vote command was not successful. Error: ", error),
            // });
        },
        error => {
            console.log('error:', error)
        }
    );
}