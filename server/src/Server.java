import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.net.*;


import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Server {
    public static void main(String[] args) throws IOException {

        try {
            File www = new File("www");
            if(!www.exists()){
                if(!www.mkdir()){
                    System.out.println("Cannot make folder www");
                    return;
                }
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
                System.out.println("New client connected");
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

        boolean isConnectionAlive = true;

        while (isConnectionAlive)
        {
            try {
                requestJSON = inputStream.readUTF();
                JSONObject requestObject = (JSONObject) JSONValue.parse(requestJSON);
                String requestType = (String)requestObject.get("type");
                switch(requestType){
                    case "GET":
                        String getFileLocation = (String)requestObject.get("target");
                        responseJSON = this.HandleGETRequest(getFileLocation);
                        outputStream.writeUTF(responseJSON);
                        break;
                    case "PUT":
                        String putFileLocation = (String)requestObject.get("target");
                        String content = (String)requestObject.get("content");
                        responseJSON = this.HandlePUTRequest(putFileLocation,content);
                        outputStream.writeUTF(responseJSON);
                        break;
                    case "DELETE":
                        String deleteFileLocation = (String)requestObject.get("target");
                        responseJSON = this.HandleDELETERequest(deleteFileLocation);
                        outputStream.writeUTF(responseJSON);
                        break;
                    case "DISCONNECT":
                        this.socket.close();
                        System.out.println("Connection closed");
                        isConnectionAlive = false;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Client Disconnected abruptly");
                isConnectionAlive = false;
            }
        }

        try
        {
            this.inputStream.close();
            this.outputStream.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String HandleGETRequest(String fileLocation){
        JSONObject responseObject=new JSONObject();
        responseObject.put("message","response");

        try {
            String pathname = "www"+fileLocation;
            File file = new File(pathname);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            String str = new String(data, StandardCharsets.UTF_8);
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

    public String HandlePUTRequest(String fileLocation, String content){
        JSONObject responseObject=new JSONObject();
        responseObject.put("message","response");

        try {
            String pathname = "www"+fileLocation;
            File myObj = new File(pathname);
            File ParentDirectory = myObj.getParentFile();
            if(ParentDirectory != null){
                if(!ParentDirectory.exists()){
                    ParentDirectory.mkdirs();
                }
            }

            if(myObj.createNewFile()){
                responseObject.put("content","Ok");
                responseObject.put("code","201");
            }else{
                responseObject.put("content","Modified");
                responseObject.put("code","202");
            }
            FileWriter myWriter = new FileWriter(pathname);
            myWriter.write(content);
            myWriter.close();
            return responseObject.toString();

        }  catch (Exception E){
            E.printStackTrace();
            responseObject.put("code","500");
            responseObject.put("content","Unknown Error");

            return responseObject.toString();
        }
    }

    public String HandleDELETERequest(String fileLocation){
        JSONObject responseObject=new JSONObject();
        responseObject.put("message","response");

        try {
            String pathname = "www"+fileLocation;
            File file = new File(pathname);
            FileInputStream fis = new FileInputStream(file);

            file.delete();
            responseObject.put("code","203");
            responseObject.put("content","Ok");

            return responseObject.toString();
        }  catch (FileNotFoundException e) {
            e.printStackTrace();

            responseObject.put("code","400");
            responseObject.put("content","Not Found");

            return responseObject.toString();
        }

    }

}