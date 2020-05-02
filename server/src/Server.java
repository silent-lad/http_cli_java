import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.util.*;
import java.net.*;


import com.sun.tools.javac.util.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.kohsuke.args4j.Option;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Server {

    @Option(name="-p",usage="Gets the port number")
    public String port;

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

        ServerSocket server = null;

        if(args.length>=2){
            int portNumber = Integer.parseInt(args[1]);
            server = new ServerSocket(portNumber);
        }else{
            server = new ServerSocket(8000);
        }


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
                requestJSON = inputStream.readLine();
                if(requestJSON==null){
                    throw new Exception();
                }
                JSONObject requestObject = (JSONObject) JSONValue.parse(requestJSON);
                String requestType = (String)requestObject.get("type");
                switch(requestType){
                    case "GET":
                        String getFileLocation = (String)requestObject.get("target");
                        responseJSON = this.HandleGETRequest(getFileLocation);
                        outputStream.write((String.valueOf(responseJSON)+"\n").getBytes("UTF-8"));
                        break;
                    case "PUT":
                        String putFileLocation = (String)requestObject.get("target");
                        String content = (String)requestObject.get("content");
                        responseJSON = this.HandlePUTRequest(putFileLocation,content);
                        outputStream.write((String.valueOf(responseJSON)+"\n").getBytes("UTF-8"));
                        break;
                    case "DELETE":
                        String deleteFileLocation = (String)requestObject.get("target");
                        responseJSON = this.HandleDELETERequest(deleteFileLocation);
                        outputStream.write((String.valueOf(responseJSON)+"\n").getBytes("UTF-8"));
                        break;
                    case "DISCONNECT":
                        this.socket.close();
                        System.out.println("Connection closed");
                        isConnectionAlive = false;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Client Disconnected");
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

        if(!this.verifyPath(fileLocation)){
            responseObject.put("code","401");
            responseObject.put("content","Bad Request");

            return responseObject.toString();
        }

        try {
            String pathname = "www"+fileLocation;
            File file = new File(pathname);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            String str = new String(data, StandardCharsets.UTF_8);
            String fileContentEscaped = StringEscapeUtils.escapeJava(str);
            responseObject.put("code","200");
            responseObject.put("content",fileContentEscaped);

            return responseObject.toString();
        }  catch (FileNotFoundException e) {
            e.printStackTrace();

            responseObject.put("code","400");
            responseObject.put("content","Not Found");

            return responseObject.toString();
        } catch (Exception e) {
            e.printStackTrace();

            responseObject.put("code","402");
            responseObject.put("content","Unknown Error");

            return responseObject.toString();
        }

    }

    public String HandlePUTRequest(String fileLocation, String content){
        JSONObject responseObject=new JSONObject();
        responseObject.put("message","response");

        if(!this.verifyPath(fileLocation)){
            responseObject.put("code","401");
            responseObject.put("content","Bad Request");

            return responseObject.toString();
        }

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
            String fileContentUnescaped = StringEscapeUtils.unescapeJava(content);
            myWriter.write(fileContentUnescaped);
            myWriter.close();
            return responseObject.toString();

        }  catch (Exception E){
            E.printStackTrace();
            responseObject.put("code","402");
            responseObject.put("content","Unknown Error");

            return responseObject.toString();
        }
    }

    public String HandleDELETERequest(String fileLocation){
        JSONObject responseObject=new JSONObject();
        responseObject.put("message","response");
        if(!this.verifyPath(fileLocation)){
            responseObject.put("code","401");
            responseObject.put("content","Bad Request");

            return responseObject.toString();
        }

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
        } catch(Exception e){
            responseObject.put("code","402");
            responseObject.put("content","Unknown Error");

            return responseObject.toString();
        }

    }

    public boolean verifyPath(String path){
        try {
            String[] arr = path.split("/");
            for (int i = 0; i < arr.length; i++) {
                if(arr[i].length()>20 && i!=arr.length-1) {
                    return false;
                }
            }
            if(arr[arr.length-1].split("\\.")[0].length()>10){
                return false;
            }

            String regex = "^[a-zA-Z0-9_/.]*$";
            if(!path.matches(regex)) return false;

            String extension = "";
            if(!path.startsWith("/")) {
                return false;
            }

            int i = path.lastIndexOf('.');
            if (i < 0) {
                return false;
            }

            extension = path.substring(i+1);
            if(extension.length()>5){
                return false;
            }

            int count = path.length() - path.replace("/", "").length();

            if (count>11){
                return false;
            }
            return true;
        }catch(Exception e){
            return false;
        }
    }

}