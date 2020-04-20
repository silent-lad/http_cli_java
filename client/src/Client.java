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
        try {
            while(true){
            Scanner scn = new Scanner(System.in);

            Boolean isConnectionEstablished = false;
            String initCommand = scn.nextLine();

            String commandType = initCommand.split(" ", 3)[0];
            String ipArg = initCommand.split(" ", 3)[1];
            String portArg = initCommand.split(" ", 3)[2];

            if (commandType.equals("connect")) {


                InetAddress IPAddress = InetAddress.getByName(ipArg);

                try {
                    Socket s = new Socket(IPAddress, Integer.parseInt(portArg));
                    DataInputStream dis = new DataInputStream(s.getInputStream());
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    isConnectionEstablished = true;
                    System.out.println("Successfully Connected");

                    while (isConnectionEstablished) {
                        String CLICommand = scn.nextLine();

                        String requestType = CLICommand.split(" ", 2)[0];
                        JSONObject obj = new JSONObject();
                        String jsonText;

                        obj.put("message", "request");

                        try {
                            switch (requestType) {
                                case "get":
                                    String targetGet = CLICommand.split(" ", 2)[1];
                                    obj.put("type", "GET");
                                    obj.put("target", targetGet);
                                    break;
                                case "put":
                                    String sourcePut = CLICommand.split(" ", 3)[1];
                                    String targetPut = CLICommand.split(" ", 3)[2];

                                    File file = new File(sourcePut);
                                    FileInputStream fis = new FileInputStream(file);
                                    byte[] data = new byte[(int) file.length()];
                                    fis.read(data);
                                    fis.close();

                                    String str = new String(data, "UTF-8");

                                    obj.put("type", "PUT");
                                    obj.put("target", targetPut);
                                    obj.put("content", str);
                                    break;
                                case "delete":
                                    String targetDelete = CLICommand.split(" ", 2)[1];
                                    obj.put("type", "DELETE");
                                    obj.put("target", targetDelete);
                                    break;
                                case "disconnect":
                                    isConnectionEstablished = false;
                                    obj.put("type", "DISCONNECT");
                                    isConnectionEstablished = false;
                                    //s.close();
                                    break;
                                default:
                                    break;
                            }

                        } catch (Exception EE) {
                            EE.printStackTrace();
                            continue;
                        }

                        try {
                            dos.writeUTF(obj.toString());
                            String responseJSON = dis.readUTF();
                            JSONObject requestObject = (JSONObject) JSONValue.parse(responseJSON);
                            String responseContent = (String) requestObject.get("content");
                            //String responseCode = (String)requestObject.get("code");
                            System.out.println(responseContent);
                        } catch (Exception socketError) {
                            if (isConnectionEstablished) {
                                System.out.println("No server");
                            } else {
                                System.out.println("Connection closed");
                            }
                            isConnectionEstablished = false;
                        }

                    }
                    // closing resources
                    dis.close();
                    dos.close();
                } catch (ConnectException e) {
                    System.out.println(e);
                }
            }

        }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
} 