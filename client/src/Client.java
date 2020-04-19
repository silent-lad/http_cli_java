// Java implementation for a client 
// Save file as Client.java 

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class 
public class Client
{
    public static void main(String[] args) throws IOException
    {
        try
        {
            Scanner scn = new Scanner(System.in);

            Boolean isConnectionEstablished = false;
            String initCommand = scn.nextLine();

            String commandType = initCommand.split(" ",3)[0];
            String ipArg = initCommand.split(" ",3)[1];
            String portArg = initCommand.split(" ",3)[2];

            if(commandType.equals("connect")){

                isConnectionEstablished = true;
                InetAddress IPAddress = InetAddress.getByName(ipArg);

                try {
                     Socket s = new Socket(IPAddress, Integer.parseInt(portArg));
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                     dos.writeUTF("YUP");

                    while (isConnectionEstablished)
                    {
                        System.out.println(dis.readUTF());
                        System.out.println("Hey YO");
                        String tosend = scn.nextLine();

                        String requestType = tosend.split(" ",2)[0];
                        switch(requestType){
                            case "connect":
                                System.out.println("Connecting");
                                break;
                            case "get":
                                System.out.println("Getting File");
                                String targetGet = tosend.split(" ",2)[1];
                                break;
                            case "put":
                                System.out.println("Putting File");
                                String sourcePut = tosend.split(" ",3)[1];
                                String targetPut = tosend.split(" ",3)[2];
                                break;
                            case "delete":
                                System.out.println("Deleting File");
                                String targetDelete = tosend.split(" ",2)[1];
                                break;
                            case "disconnect":
                                isConnectionEstablished=false;
                                System.out.println("Closing this connection : " + s);
                                s.close();
                                System.out.println("Connection closed");
                                break;
                            default:
                                break;
                        }
                        dos.writeUTF(tosend);

                        // printing date or time as requested by client
                        String received = dis.readUTF();
                        System.out.println(received);
                    }
                    // closing resources
                    scn.close();
                    dis.close();
                    dos.close();
                }catch(ConnectException e){
                    System.out.println(e);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
} 