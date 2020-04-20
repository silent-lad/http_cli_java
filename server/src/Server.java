import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;


import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Server {
    public static void main(String args[]) throws IOException {

        try {
            File www = new File("www");
            if(!www.exists()){
                www.mkdir();
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        int serverPort = 8000;
        ServerSocket server = new ServerSocket(serverPort);
        while (true) {
            Socket socket = null;
            try {
                socket = server.accept();
                System.out.println("Server application running at port 8000");
                Date today = new Date();
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                Thread ConnectionThread = new Connection(socket, inputStream, outputStream);

                ConnectionThread.start();
            } catch (Exception e) {
                socket.close();
                System.out.println("New client could not be connected");
            }
        }

    }
}
class Connection extends Thread
{
    final DataInputStream inputStream;
    final DataOutputStream outputStream;
    final Socket socket;


    public Connection(Socket socket, DataInputStream inputStream, DataOutputStream outputStream)
    {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    @Override
    public void run()
    {
        String responseJSON;
        String requestJSON ;

        Boolean isConnectionAlive = true;

        while (isConnectionAlive)
        {
            try {
                requestJSON = inputStream.readUTF();
                System.out.println(requestJSON);
                JSONObject requestObject = (JSONObject) JSONValue.parse(requestJSON);
                String requestType = (String)requestObject.get("type");
                switch(requestType){
                    case "GET":
                        String getFileLocation = (String)requestObject.get("target");
                        System.out.println(getFileLocation);
                        responseJSON = this.HandleGETRequest(getFileLocation);
                        outputStream.writeUTF(responseJSON);
                }
                System.out.println(requestType);

                if(requestType.equals("Exit"))
                {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                //outputStream.writeUTF(requestType);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Client Disconnected abruptly");
                isConnectionAlive = false;
            }
        }

        try
        {
            // closing resources
            this.inputStream.close();
            this.outputStream.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String HandleGETRequest(String fileLocation){
        JSONObject responseObject=new JSONObject();
        responseObject.put("message","response");
//        try {
//            String pathname = "www"+fileLocation;
//            File myObj = new File(pathname);
//            if (myObj.createNewFile()) {
//                System.out.println("File created: " + myObj.getName());
//            } else {
//                System.out.println("File already exists.");
//            }
//            responseObject.put("code","200");
//            responseObject.put("content","Ok");
//        }catch (IOException E){
//            E.printStackTrace();
//        }
        try {
            String pathname = "www"+fileLocation;
            File file = new File(pathname);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            String str = new String(data, "UTF-8");
            responseObject.put("code","200");
            responseObject.put("content",str);

            return responseObject.toString();
        }  catch (FileNotFoundException e) {
            e.printStackTrace();

            responseObject.put("code","400");
            responseObject.put("content","Not Found");

            return responseObject.toString();
        } catch (IOException e) {
            e.printStackTrace();

            responseObject.put("code","500");
            responseObject.put("content","Unknown Error");

            return responseObject.toString();
        }

    }
}