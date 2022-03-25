import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;


public class ChatServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("CHAT SERVER");
        int portNumber = 12345;
        int multicastPortNumber = 54321;
        int maxClients = 10;
        int clientCounter = 0;

        // UDP socket
        ServerUDPThread serverUDPThread = new ServerUDPThread(portNumber);
        serverUDPThread.start();


        // Multicast socket
        InetAddress group = InetAddress.getByName("229.0.0.0");
        ServerMulticastThread serverMulticastThread = new ServerMulticastThread(multicastPortNumber, group);
        serverMulticastThread.start();


        // TCP socket
        PrintWriter[] outputStreamArray = new PrintWriter[maxClients];
        try (ServerSocket serverSocketTCP = new ServerSocket(portNumber)) {
            while (clientCounter < maxClients) {
                // accept client
                Socket clientSocket = serverSocketTCP.accept();
                outputStreamArray[clientCounter] = new PrintWriter(clientSocket.getOutputStream(), true);
                new ServerTCPThread(clientSocket, outputStreamArray, clientCounter++).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        serverUDPThread.join();
        serverMulticastThread.join();
    }
}
