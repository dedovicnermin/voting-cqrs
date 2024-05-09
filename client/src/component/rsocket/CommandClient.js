import {RSocketClient, MESSAGE_RSOCKET_COMPOSITE_METADATA, BufferEncoders} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';

// Define the connection options
const client = new RSocketClient({
    setup: {
        keepAlive: 60000,             // ms between keep alive frames
        lifetime: 180000,             // ms connection lifetime
        dataMimeType: 'application/json',
        metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
    },
    transport: new RSocketWebSocketClient({
        debug: true,
        // url: `${process.env.REACT_APP_CMD_ENDPOINT}`  // WebSocket URL of the RSocket server
        url: 'ws://localhost:7000/cmd', // WebSocket URL of the RSocket server
        wsCreator: url => new WebSocket(url)
    }, BufferEncoders),
});



export default client