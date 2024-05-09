const { RSocketClient, JsonSerializer} = require('rsocket-core');
const RSocketWebsocketClient = require('rsocket-websocket-client').default;
const WebSocket = require('ws');

function now() {
    return new Date().getTime();
}

async function connect(options) {
    const transportOptions = {
        url: 'ws://localhost:7000/cmd',
        wsCreator: (url) => {
            return new WebSocket(url);
        },
    };
    const setup = {
        keepAlive: 1000000,
        lifetime: 100000,
        dataMimeType: 'application/json',
        metadataMimeType: 'messaging/key',
    };
    const transport = new RSocketWebsocketClient(transportOptions);
    const client = new RSocketClient({ setup, transport });
    return await client.connect();
}

// export async function fireVote(key, value) {
//     return new Promise(async (resolve, reject) => {
//         const rsocket = await connect();
//         const start = now();
//
//
//     })
// }