// Java implementation for a client 
// Save file as Client.java 

import java.io.*;
import java.net.*;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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

                    while (isConnectionEstablished)
                    {
                        String tosend = scn.nextLine();

                        String requestType = tosend.split(" ",2)[0];
                        JSONObject obj = new JSONObject();
                        String jsonText;

                        obj.put("message", "request");


                        try{switch(requestType){
                            case "connect":
                                System.out.println("Connecting");
                                break;
                            case "get":
                                String targetGet = tosend.split(" ",2)[1];
                                obj.put("type", "GET");
                                obj.put("target", targetGet);
                                break;
                            case "put":
                                System.out.println("Putting File");
                                String sourcePut = tosend.split(" ",3)[1];
                                String targetPut = tosend.split(" ",3)[2];
                                obj.put("type", "PUT");
                                obj.put("target", targetPut);
                                obj.put("source", sourcePut);
                                break;
                            case "delete":
                                System.out.println("Deleting File");
                                String targetDelete = tosend.split(" ",2)[1];
                                obj.put("type", "DELETE");
                                obj.put("target", targetDelete);
                                break;
                            case "disconnect":
                                isConnectionEstablished=false;
                                obj.put("type", "DISCONNECT");
                                System.out.println("Closing this connection : " + s);
                                s.close();
                                System.out.println("Connection closed");
                                break;
                            default:
                                break;
                        }
                        dos.writeUTF(obj.toString());
                        String received = dis.readUTF();
                        System.out.println(received);}catch(Exception EE){

                        }
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