import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;

public class Client {

    public static void main(String[] args) throws IOException {

        System.out.println("CLIENT START");
        String hostName = "localhost";

        // Ports
        int portNumber = 12345;
        int multicastPortNumber = 54321;

        // Sockets
        Socket socketTCP = null;
        DatagramSocket socketUDP = null;
        MulticastSocket multicastSocket = null;

        // Addresses
        InetAddress address = InetAddress.getByName("localhost");
        InetAddress group = InetAddress.getByName("229.0.0.0");


        try {
            // create TCP socket
            socketTCP = new Socket(hostName, portNumber);


            // create UDP socket
            socketUDP = new DatagramSocket();
            byte[] sendBuffer = "connect".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
            socketUDP.send(sendPacket);


            // create Multicast socket
            multicastSocket = new MulticastSocket(multicastPortNumber);
            multicastSocket.joinGroup(group);

            // in & out streams
            PrintWriter out = new PrintWriter(socketTCP.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socketTCP.getInputStream()));


            // stdIn
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));


            // reading from server TCP
            Thread readingFromServerTCPThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String fromServer;
                    try {
                        while ((fromServer = in.readLine()) != null) {
                            System.out.println(fromServer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            readingFromServerTCPThread.start();

            // reading from server UDP
            DatagramSocket finalSocketUDP = socketUDP;
            Thread readingFromServerUDPThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] receiveBuffer = new byte[1024];
                    try {
                        while (true) {
                            Arrays.fill(receiveBuffer, (byte) 0);
                            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                            finalSocketUDP.receive(receivePacket);
                            String fromServer = new String(receiveBuffer, 0, receivePacket.getLength());
                            System.out.println(fromServer);
                        }

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            readingFromServerUDPThread.start();


            // reading from Multicast
            MulticastSocket finalMulticastSocket = multicastSocket;
            Thread readingFromMulticastThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] receiveBuffer = new byte[1024];
                    try {
                        while (true) {
                            Arrays.fill(receiveBuffer, (byte) 0);
                            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                            finalMulticastSocket.receive(receivePacket);
                            String fromMulticast = new String(receivePacket.getData(), 0, receivePacket.getLength());
                            System.out.println("Multicast: " + fromMulticast);
                        }

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            readingFromMulticastThread.start();



            // reading from user
            Thread readingFromUserThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String fromUser;
                    byte[] sendBuffer = ("""
                                                     ||  || \s
                                                     \\\\()//\s
                                                    //(__)\\\\
                                                    ||    ||""").getBytes();

                    try {
                        while ((fromUser = stdIn.readLine()) != null) {
                            if(fromUser.equals("U")){
                                // send UDP
                                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
                                finalSocketUDP.send(sendPacket);
                            } else if(fromUser.equals("M")){
                                // send multicast
                                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, group, multicastPortNumber);
                                finalMulticastSocket.send(sendPacket);
                            } else{
                                out.println(fromUser);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            readingFromUserThread.start();

            readingFromServerTCPThread.join();
            readingFromServerUDPThread.join();
            readingFromMulticastThread.join();
            readingFromUserThread.join();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socketTCP != null) {
                socketTCP.close();
            }
            if (socketUDP != null){
                socketUDP.close();
            }
            if (multicastSocket != null){
                multicastSocket.leaveGroup(group);
                multicastSocket.close();
            }
        }
    }

}
