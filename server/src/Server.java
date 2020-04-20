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
            File myObj = new File("filename.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
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
                System.out.println(requestType);

                if(requestType.equals("Exit"))
                {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                outputStream.writeUTF(requestType);
            } catch (Exception e) {
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
}