import net = require("net");
import dgram = require("dgram");
import fs = require("fs");
import readline = require("readline");
import {fromEvent, partition} from  "rxjs";
import {scan, delay, filter, map, catchError} from "rxjs/operators";

function reconnect(socket: net.Socket, triesLeft: number){
  if(triesLeft === 0) socket.destroy();
  else {
    console.log("Reconnecting...")
    socket.connect({port: 8080, host: "localhost"});
  }
}

function transmitFile(path: string, socket: dgram.Socket){
    const readStream = fs.createReadStream(path);
    readStream.on("data", (chunk) => {
      socket.send(chunk, 8080, "localhost");
    });
    readStream.on("error", (err) => {
      console.log("Transmission error: ");
      console.log(err.message);
    });
}

const tcpSocket = net.createConnection({
  port: 8080,
  host: "localhost",
  allowHalfOpen: true
});

const udpSocket = dgram.createSocket({type: "udp4", reuseAddr: true});
udpSocket.bind(8081, "localhost", () => {
  udpSocket.setBroadcast(true);
});

udpSocket.on('error', (err) => console.error(err));
udpSocket.on("message", (msg, rinfo) => console.log(msg.toLocaleString()));

tcpSocket.on("error", (error) => {
  console.log(`Connection error: ${error}`)
})
tcpSocket.on("data", data => console.log(data.toLocaleString()));
tcpSocket.on("end", () => {
  console.log("Server is down. Closing connection...");
  tcpSocket.destroy();
});

const [fails, finalClosing] = partition(fromEvent(tcpSocket, "close"),
                                   hadError => hadError as boolean);

fails.pipe(scan(count => count - 1, 5), delay(1000))
  .subscribe(tries => reconnect(tcpSocket, tries));

finalClosing.subscribe(() => {
  console.log("Terminating...");
  process.exit(0);
})

const io = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

const [commands, messages] = partition(fromEvent<string>(io, "line"),
                                       line => line.startsWith("!"));

messages.subscribe(line => {
  readline.moveCursor(process.stdout, 0, -1);
  console.log("[You]>> " + line);
  tcpSocket.write(line); 
  tcpSocket.write(line);
})

commands.pipe(
  filter(cmd => cmd.startsWith("!U")),
  map(cmd => cmd.split(" ")[1]))
  .subscribe(filePath => transmitFile(filePath, udpSocket));