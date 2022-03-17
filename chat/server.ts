import net = require("net");
import dgram = require("dgram");
import {Worker} from "worker_threads";

interface Client {
    worker: Worker,
    socket: net.Socket,
    id: number
}

interface Message {
    content: string,
    senderId: number
}

function makeClient(socket: any, clients: Client[]): Client{
    const clientId = clients.length === 0 ? 0 : clients[clients.length-1].id + 1;
    const clientWorker = new Worker("./client-worker.js", 
                                    { workerData: {
                                        socketFd: socket._handle.fd,
                                        id: clientId
                                    }
                                });
    clientWorker.on("message", (msg: Message) => {
        clients.forEach(client => {
            if(client.id !== msg.senderId){
                client.worker.postMessage(msg);
            }            
        });
    })

    clientWorker.on("exit", () => {
        clients = clients.filter(c => c.id !== clientId);
        socket.destroy();
    });

    socket.write("Connected successfully.");
    console.log(`User id=${clientId} connected.`)
    
    return {
        worker: clientWorker,
        socket: socket,
        id: clientId
    }
}

const connectionOptions = {
    host: "localhost",
    port: 8080,
    allowHalfOpen: true
}

let clients: Client[] = [];

const tcpServer = net.createServer();
const udpSocket = dgram.createSocket("udp4");

tcpServer.on("listening", () => console.log("[TCP] Server is running..."))
tcpServer.on("connection", (socket) => {
    clients.push(makeClient(socket, clients))
});

udpSocket.on("message", (msg, rinfo) => {
    console.log(`Received ${rinfo.size} bytes datagram.`);
    udpSocket.send(msg, 8081, "localhost");
})
udpSocket.on("error", (err) => console.error(err));

tcpServer.listen(connectionOptions);

udpSocket.bind(8080, "localhost", () => {
    console.log(
        `[UDP] Listening at:
         ${udpSocket.address().address}:${udpSocket.address().port}`
         );
    udpSocket.setBroadcast(true);
});