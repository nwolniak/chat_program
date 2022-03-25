import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class ServerMulticastThread extends Thread {
    private final MulticastSocket serverMulticastSocket;
    private final InetAddress group;
    private final byte[] receiveBuffer = new byte[1024];

    public ServerMulticastThread(int portNumber, InetAddress group) throws IOException {
        super("ServerMulticastThread");
        this.serverMulticastSocket = new MulticastSocket(portNumber);
        this.group = group;
        this.serverMulticastSocket.joinGroup(this.group);
    }

    public void run(){
        try {
            while (true) {
                Arrays.fill(this.receiveBuffer, (byte) 0);
                DatagramPacket receivePacket = new DatagramPacket(this.receiveBuffer, this.receiveBuffer.length);
                this.serverMulticastSocket.receive(receivePacket);
                String fromMulticast = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("Multicast: " + fromMulticast);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (this.serverMulticastSocket != null) {
                try {
                    this.serverMulticastSocket.leaveGroup(this.group);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.serverMulticastSocket.close();
            }
        }
    }

}
