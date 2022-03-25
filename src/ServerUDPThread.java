import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ServerUDPThread extends Thread {
    private final DatagramSocket serverSocketUDP;
    private final int maxClients = 10;
    private final byte[] receiveBuffer = new byte[1024];
    private final InetAddress[] addresses = new InetAddress[maxClients];
    private final int[] clientPorts = new int[maxClients];

    public ServerUDPThread(int portNumber) throws SocketException {
        super("ServerTCPThread");
        this.serverSocketUDP = new DatagramSocket(portNumber);
    }

    public void run() {
        try {
            while (true) {
                // Receive msg
                Arrays.fill(this.receiveBuffer, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(this.receiveBuffer, this.receiveBuffer.length);
                this.serverSocketUDP.receive(receivePacket);


                // Add client
                InetAddress address = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                System.out.println("UDP Received packet from Address:" + address + " Port:" + clientPort);

                boolean isFreePlace = false;
                int freePlaceIdx = 0;
                for (int i = 0; i < maxClients; i++) {
                    if (this.addresses[i] != null && this.clientPorts[i] != 0) {
                        if (this.addresses[i].equals(address) && this.clientPorts[i] == clientPort) {
                            isFreePlace = false;
                            break;
                        }
                    } else if (!isFreePlace){
                        isFreePlace = true;
                        freePlaceIdx = i;
                    }
                }
                if (isFreePlace) {
                    this.addresses[freePlaceIdx] = address;
                    this.clientPorts[freePlaceIdx] = clientPort;
                }

                // Connect to server
                String fromClient = new String(receiveBuffer, 0, receivePacket.getLength());
                if (fromClient.equals("connect")) continue;


                // Sender ID
                int senderID = 0;
                for (int i = 0 ; i < maxClients ; i++){
                    if (this.addresses[i].equals(address) && this.clientPorts[i] == clientPort){
                        senderID = i;
                        break;
                    }
                }


                // Send response to clients
                for (int i = 0; i < maxClients; i++) {
                    if (this.addresses[i] != null && this.clientPorts[i] != 0 && i != senderID) {
                        byte[] sendBuffer = ("Client" +  senderID + ": \n" + fromClient).getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, this.addresses[i], this.clientPorts[i]);
                        this.serverSocketUDP.send(sendPacket);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (this.serverSocketUDP != null) {
                this.serverSocketUDP.close();
            }
        }
    }

}
