import React, { createContext, useContext, useEffect, useState } from 'react';
import {
    BufferEncoders,
    encodeCompositeMetadata, encodeRoute,
    MESSAGE_RSOCKET_COMPOSITE_METADATA,
    MESSAGE_RSOCKET_ROUTING,
    RSocketClient
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';

const RSocketContext = createContext(null);

export const RSocketProvider = ({ children }) => {
    const [client, setClient] = useState(null);

    useEffect(() => {
        // Initialize RSocket client
        const rsocketClient = new RSocketClient({
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

        // Connect to RSocket server
        rsocketClient.connect().subscribe({
            onComplete: socket => {
                console.log('Connection established');
                setClient(socket);
            },
            onError: error => console.error('Connection has failed', error),
            onSubscribe: cancel => { /* handle cancellation if needed */ },
        });

        return () => {
            rsocketClient.close();
        };
    }, []);

    const sendFireAndForget = (route, key, value) => {
        if (client) {
            client.fireAndForget({
                data: Buffer.from(JSON.stringify(value)),
                metadata: encodeCompositeMetadata([
                    [MESSAGE_RSOCKET_ROUTING, encodeRoute(route)],
                    ['messaging/key', Buffer.from(key)]
                ])
            });
        } else {
            console.error('RSocket client is not connected');
        }
    };

    return (
        <RSocketContext.Provider value={{ sendFireAndForget }}>
            {children}
        </RSocketContext.Provider>
    );
};

export const useRSocket = () => useContext(RSocketContext);
