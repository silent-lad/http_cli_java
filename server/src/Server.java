import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;


import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Server {
    public static void main(String args[]) throws IOException {
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
        String received;
        String toreturn;
        while (true)
        {
            try {

                // Ask user what he wants
                outputStream.writeUTF("What do you want?[Date | Time]..\n"+
                        "Type Exit to terminate connection.");

                // receive the answer from client
                received = inputStream.readUTF();
                if(received.equals("Exit"))
                {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }
                outputStream.writeUTF(received);
            } catch (IOException e) {
                e.printStackTrace();
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