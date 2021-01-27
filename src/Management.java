import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Management {


    private Transport sendAudioTransport = new Transport();
    private ArrayList<Transport> getAudioTransports =  new ArrayList<>();


    public void init(int port){

        /*
        listen = Use when you want get voice sent to a port [listen=port]
        stop-listen = Use when you don't want the sound anymore [stop-listen=]
        speak = Use when you want the agent to play a received byte for your [speak=port]
        stop-speak = Use when you want to terminate voice being spoken for a specific port [stop-speak=port] (Use some previously used speak port)
        */

        try {

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Starting server on " + port);

            while (true){
                Socket client = serverSocket.accept();
                System.out.println("Incoming message ...");
                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            StringBuilder stringBuilder = new StringBuilder();

                            byte data[] = new byte[4096];
                            int size;
                            while ((size = dataInputStream.read(data, 0, data.length)) != -1){
                                System.out.println("CLient: " + client.getPort());
                                System.out.println("Getting Data ... (Size: " + size + " )");
                                String inData = new String(data, StandardCharsets.US_ASCII).replaceAll("\\p{C}", "");;
                                System.out.println("Fetched " + inData);
                                stringBuilder.append(inData);
                                System.out.println("Appended!");
                            }

                            String message = stringBuilder.toString();
                            System.out.println("Reading Message: " + message);

                            String Actions[] = message.split("=");
                            // ACTION_NAME=CPort


                            if(Actions[0].equals("listen")){
                                System.out.println("INPUT STRING: (" + Actions[1] + ")");
                                sendAudio(Integer.parseInt(Actions[1]));
                            }else if(Actions[0].equals("stop-listen")){
                                stopAudio();
                            }else if(Actions[0].equals("speak")){
                                getAudio(Integer.parseInt(Actions[1]));
                            }else if(Actions[0].equals("stop-speak")){
                                terminateAudio(Integer.parseInt(Actions[1]));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendAudio(int port){

        try {
            TargetDataLine microphone = AudioAgent.getMicrophone();
            Socket senderAgent = new Socket("127.0.0.1", port);
            DataOutputStream dataOutputStream = new DataOutputStream(senderAgent.getOutputStream());

            sendAudioTransport.setDataOutputStream(dataOutputStream);
            sendAudioTransport.setSocket(senderAgent);
            sendAudioTransport.setActive();
            sendAudioTransport.setPort(port);

            System.out.println("Audio Transport Is: " + sendAudioTransport.isActive());


            byte buffer[] = new byte[microphone.getBufferSize()/5];
            while (true){
                System.out.println("Writing Data ...");
                int dataCount = microphone.read(buffer, 0, buffer.length);
                if(dataCount != -1){
                    dataOutputStream.write(buffer, 0, buffer.length);
                }
            }

        } catch (SocketException e) {
            System.out.println("Socket Closed!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopAudio(){
        System.out.println("Stop Audio Called");
        try {
            System.out.println("Audio Transport Is: " + sendAudioTransport.isActive());
            if(sendAudioTransport.isActive()){
                System.out.println("Closing send audio socket!");
                sendAudioTransport.getDataOutputStream().close();
                sendAudioTransport.getSocket().close();
            }else {
                System.out.println("Send Audio Transport is not active!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sendAudioTransport.setInActive();
        }
    }

    private void getAudio(int port){

        try {
            Transport getAudioTransport = new Transport();

            ServerSocket audioListener = new ServerSocket(port);
            SourceDataLine speaker = AudioAgent.getSpeaker();

            getAudioTransport.setServerSocket(audioListener);

            while (true){

                // If this happens twice it means that there was an issue connecting to the local voice system
                // So on this occasion we must reset the socket and it's stream reader for the new ones incoming so we close
                // The previous ones and set the new ones upon creation
                if(getAudioTransport.getDataInputStream() != null) getAudioTransport.getDataInputStream().close();
                if(getAudioTransport.getSocket() != null) getAudioTransport.getSocket().close();

                Socket audioCatcher = audioListener.accept();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataInputStream audioDataStream = new DataInputStream(audioCatcher.getInputStream());

                            getAudioTransport.setSocket(audioCatcher);
                            getAudioTransport.setDataInputStream(audioDataStream);

                            getAudioTransport.setActive();

                            // Make sure that the Transport object is always up to date in the arrayList
                            if(getAudioTransport(port) != -1) {
                                int index = getAudioTransport(port);
                                getAudioTransports.remove(index);
                            }

                            getAudioTransports.add(getAudioTransport);

                            byte[] buffer = new byte[1024];

                            while (true){

                                int dataCount = audioDataStream.read(buffer, 0, buffer.length);
                                if(dataCount != -1){
                                    speaker.write(buffer, 0, buffer.length);
                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void terminateAudio(int port){
        System.out.println("Terminating Get Audio");
        int index = getAudioTransport(port);
        if(index != -1){
            try {

                System.out.println("Transport Removed: " + index);
                Transport transport = getAudioTransports.get(index);
                transport.getDataInputStream().close();
                transport.getServerSocket().close();
                transport.getSocket().close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                getAudioTransports.remove(index);
            }

        }
    }

    private int getAudioTransport(int port){
        for (Transport getAudioTransport: getAudioTransports) {
            if(getAudioTransport.getPort() == port) getAudioTransports.indexOf(getAudioTransport);
        }

        return -1;
    }

}
