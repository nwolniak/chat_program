import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class ServerTCPThread extends Thread {
    private final Socket clientSocket;
    private final PrintWriter[] outputStreamArray;
    private final int id;

    public ServerTCPThread(Socket clientSocket, PrintWriter[] outputStreamArray , int id) {
        super("ServerTCPThread");
        this.clientSocket = clientSocket;
        this.outputStreamArray = outputStreamArray;
        this.id = id;
    }

    public void run() {
        try {
            // client address, port
            InetAddress address = this.clientSocket.getInetAddress();
            int port = this.clientSocket.getPort();
            System.out.println("TCP Connection from Address:" + address + " Port:" + port);

            // in streams
            BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            // read msg
            String fromClient;
            while((fromClient = in.readLine()) != null){
                System.out.println("TCP Received packet from Address:" + address + " Port:" + port);
                if (fromClient.equals("exit")) break;

                // send response
                for(int i = 0 ; i < 10 ; i++){
                    if (this.outputStreamArray[i] != null && i != this.id){
                        this.outputStreamArray[i].println("Client" + id + ": " + fromClient);
                    }
                }
            }

            // close socket
            this.clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (this.clientSocket != null) {
                try {
                    this.clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
