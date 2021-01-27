
const { spawn } = require('child_process');
const net = require('net');

class Mic{

    listeners = [];
    players= [];
    types = {
        "LISTEN":1,
        "PLAY":2
    }
    commands = {
        "COMMAND_REQUEST_LISTEN":"listen",
        "COMMAND_REQUEST_STOP_LISTEN":"stop-listen",
        "COMMAND_REQUEST_SPEAK":"speak",
        "COMMAND_REQUEST_STOP_SPEAK":"stop-speak",

    }

    constructor () {

        console.log("[MIC] Setting up java audio agent ...")
        const AudioAgent = spawn("java", ["-jar", "./bin/AudioAPI.jar"])

        AudioAgent.stdout.on("data", (data) => {
            console.log(`[MIC AGENT]: ${data}`)
        })

        AudioAgent.on("exit", (code) => {
            console.log(`[MIC AGENT]: Agent Closed, Code: ${code}`)
        })

        AudioAgent.stderr.on("data", (error) => {
            console.log(`[MIC AGEN]: Error: ${error}`)
        })
        
    }

    /**
     * @deprecated
     * @description Does nothing, probably not usable for any case imo, was created on mistake but is still here (idk why :D)
    */
    init = (host, port, onData, onError, onEnd) => {

        net.createServer((socket) => {

            console.log("[MIC]: Data channel initilized on port " + port)

            socket.on("data", onData)
            socket.on("error", onError)
            socket.on("end", onEnd)

        }, host, port);

    }

    /**
     * @deprecated
     * @description Send a message to a server on a certain port (this is not used either so i guess it's gonna be removed soon!)
    */
    message = (host, port, buffer) => {

        return new Promise((res, rej) => {

            const connection = net.createConnection({host:host, port:port}, () => {

            }).on("connect", () => {
                connection.write(buffer, (error) => {
                    if(error) rej(error);
                    res();
                })
            }).on("timeout", () => {
                rej({
                    code:-1,
                    message:"Connection timed out!"
                })
            })

        })
    }


    /**
     * @description Send a command to voice agent, check commands param for more command Types
     * @param host Optional hostname where the app is running (127.0.0.1 should be used mostly)
     * @param command Use commands for specify certain command that the agnet accepts!
     * @param Targetport The port that your command carries which tells the agent wherther to open that port or send data to it.
    */
    command = (host="127.0.0.1", command, Targetport) => {

        return new Promise((res, rej) => {
            const connection = net.createConnection({port:7000, host:host}, () => {
                console.log(`Connecting to server for command ...`)
            }).on("connect", () => {
                // connection.setEncoding('utf8')
                connection.write(`${command}=${Targetport}`, "ascii", (error) => {
                    if(error) {
                        console.log(`Error: ${error}`)
                        connection.destroy()
                        rej()
                    }
                    else
                    {
                        console.log("Command Sent.")
                        connection.destroy()
                        res()
                    }
                })
            })
        })

    }

    /**
     * @description Listen for incoming data on a certain port
     * @param host Hostname
     * @param port desired port
     * @param onCreated called when the listener id deployed
     * @param onData
    */
    listen = (host, port, onCreated, onData, onError) => {

        const listener = net.createServer(async (socket) => {

            console.log("Created server on: " + port)

            socket.on("data", (data) => {
                // console.log("Voice Data: " + data)
                onData(data);
            })


        });
        
        listener.on("error", (error) => {
            onError(error)
        })

        listener.listen(port, host);
        onCreated();

        const functions = {
            close: () => {
                listener.close();
            }
        }

        return functions;


    }

    play = (host, port) => {

        return new Promise((res, rej) => {
            const connection = net.createConnection({port:port, host:host}, () => {
                console.log(`Connecting to local agent ...`)
            }).on("connect", () => {
    
                const player = {
                    canPlay:true,
                    doPlay: (data) => {
                        if(player.canPlay) connection.write(data);
                    },
                    stopPlay: () => {
                        connection.destroy();
                        player.canPlay = false;
                    }
                }

                res(player);
    
            }).on("timeout", () => {
                rej();
            })
        })

    }

}

module.exports = Mic;