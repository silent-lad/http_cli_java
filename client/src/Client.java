// Java implementation for a client 
// Save file as Client.java 

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


// Client class
// 115.146.85.127
// 80
public class Client
{
    public static void main(String[] args) throws IOException
    {
        while(true)
        {
            Scanner scn = new Scanner(System.in);

            boolean isConnectionEstablished = false;
            String initCommand = scn.nextLine();

            if(initCommand.split(" ",3).length!=3){
                continue;
            }

            String commandType = initCommand.split(" ", 3)[0];
            String ipArg = initCommand.split(" ", 3)[1];
            String portArg = initCommand.split(" ", 3)[2];

            if (commandType.equals("connect")) {

                Socket s = null;
                DataInputStream dis = null;
                DataOutputStream dos = null;

                try {
                    InetAddress IPAddress = InetAddress.getByName(ipArg);
                     s = new Socket(IPAddress, Integer.parseInt(portArg));
                     dis = new DataInputStream(s.getInputStream());
                     dos = new DataOutputStream(s.getOutputStream());
                     isConnectionEstablished = true;
                     System.out.println("Successfully Connected");
                } catch (Exception e) {
                    System.out.println("No Server at the given port and IP");
                    continue;
                }

                while (isConnectionEstablished) {
                    String CLICommand = scn.nextLine();

                    String requestType = CLICommand.split(" ", 2)[0];
                    JSONObject obj = new JSONObject();

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
                                try {
                                    File file = new File(sourcePut);
                                    FileInputStream fis = new FileInputStream(file);
                                    byte[] data = new byte[(int) file.length()];
                                    fis.read(data);
                                    fis.close();

                                    String str = new String(data, StandardCharsets.UTF_8);
                                    String fileContentEscaped = StringEscapeUtils.escapeJava(str);
                                    obj.put("type", "PUT");
                                    obj.put("target", targetPut);
                                    obj.put("content", fileContentEscaped);
                                    break;
                                }catch(FileNotFoundException fileMissing){
                                    System.out.println("Source file Not Found");
                                    continue;
                                }
                            case "delete":
                                String targetDelete = CLICommand.split(" ", 2)[1];
                                obj.put("type", "DELETE");
                                obj.put("target", targetDelete);
                                break;
                            case "disconnect":
                                obj.put("type", "DISCONNECT");
                                isConnectionEstablished = false;
                                //s.close();
                                break;
                            default:
                                continue;
                        }

                    } catch (Exception wrongCommandIgnored) {
                        continue;
                    }

                    try {
                        dos.write((String.valueOf(obj)+"\n").getBytes("UTF-8"));
                        String line = dis.readLine();
                        if(line==null){
                            throw new Exception();
                        }
                        JSONObject requestObject = (JSONObject) JSONValue.parse(line);
                        String responseContent = (String) requestObject.get("content");
                        String responseContentUnescaped = StringEscapeUtils.unescapeJava(responseContent);
                        System.out.println(responseContentUnescaped);
                    } catch (Exception someException) {
                        System.out.println("Successfully disconnected");
                        dis.close();
                        dos.close();
                        s.close();
                    }

                }
            }

        }
    }
}