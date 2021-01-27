var mic = require('./AudioAPI.js');

// Create a new instance of mic class and name it as make
var make = new mic();

// Ask the local java audio agent to open a port for you to play sent bytes to it.
make.command("127.0.0.1", make.commands.COMMAND_REQUEST_SPEAK, 8001).then(() => {

    // Ask the local java audio agent to start sending microphone data to this port.
    make.command("127.0.0.1", make.commands.COMMAND_REQUEST_LISTEN, 9090).then(() => {

        // Create an audio player which will be sending passed audio data to the targeted port
        var sender = make.play("localhost", 8001)
        sender.then((player) => {
            // Once you have the sender, it will provide you with a player object which has doPlay(data) and stopPlay() functions to control the player
            // Now we create a listener for incoming audio data from agent
            const listener = make.listen("localhost", "9090", () => {
                //Here, it means that the audio listener is created and is ready to be used.

                // We stop the playback after 10 seconds
                setTimeout(() => {

                    // Ask the agent to stop sending mic data to port 9090 which was previously opened!
                    make.command("127.0.0.1", make.commands.COMMAND_REQUEST_STOP_LISTEN, 9090).then(() => {

                        //Ask the agent to stop accepting audio data for playback
                        make.command("127.0.0.1", make.commands.COMMAND_REQUEST_STOP_SPEAK, 8001).then(() => {
                            // Stop the player
                            player.stopPlay();
                            // Close the audio listener which was being received port 8001 (which you closed by the last command)
                            listener.close();
                        })
                    })
                    
                    
                }, 10000);

            
            }, (data) => {

                // When a new incoming data is being received from agent (audio data), you can ask the player to play the received data using player.doPlay(data)
                player.doPlay(data)

            }, (error) => {
                // In case any errors happens, this will be shown here!
                console.log(`Error: ${error}`)

            })
                
        }).catch(() => {
            // If there was a socket error
        })

    }).catch((errorObject) => {
        // Getting Error Object
    })

}).catch((errorObject) => {
    // Getting Error Object
})



