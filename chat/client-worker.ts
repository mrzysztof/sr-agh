import net = require("net");
import {parentPort, workerData} from "worker_threads";

interface Message {
    content: string,
    senderId: number
}

function makeMessage(content: string, id: number): Message{
    return {
        content: "[" + id.toString() + "]>> " + content,
        senderId: id
    }
}

const socket: net.Socket = new net.Socket({
    fd: workerData.socketFd,
    readable: true,
    writable: true,
    allowHalfOpen: true
});

const id: number = workerData.id;

socket.on("data", (data: string) => {
    parentPort?.postMessage(makeMessage(data, id));
});

socket.on("end", () => {
    console.log(`Client id=${id} disconnected.`);
    process.exit(0);
});

parentPort?.on("message", (msg: Message) => {
    socket.write(msg.content);
});
