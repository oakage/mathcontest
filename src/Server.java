import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

public class Server
{
    private static final int port = 4444;

    private int serverPort;
    private List<ReadThread> clients;
    private List<String> users;
    public static void main(String[] args)
    {
        Server server = new Server(port);
        server.startServer();
    }

    public Server(int portNumber){
        this.serverPort = portNumber;
    }

    public List<ReadThread> getClients(){
        return clients;
    }

    private void startServer(){
        clients = new ArrayList<ReadThread>();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
            acceptClients(serverSocket);
        } catch (IOException e){
            System.err.println("Could not listen on port: "+serverPort);
            System.exit(1);
        }
    }

    private void acceptClients(ServerSocket serverSocket){

        System.out.println("server starts port = " + serverSocket.getLocalSocketAddress());
        while(true){
            try{
                Socket socket = serverSocket.accept();
                System.out.println("accepts : " + socket.getRemoteSocketAddress());
                //int ip = Integer.parseInt(socket.getRemoteSocketAddress().toString());
                //String ips = Integer.toString(ip);
                ReadThread client = new ReadThread(this, socket);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
                //users.add(ips);
            } catch (IOException ex){
                System.out.println("Accept failed on : "+serverPort);
            }
        }
    }

}

class ServerThread implements Runnable {
    private Socket socket;
    private String userName;
    private boolean isAlived;
    private final LinkedList<String> messagesToSend;
    private boolean hasMessages = false;

    public ServerThread(Socket socket, String userName){
        this.socket = socket;
        this.userName = userName;
        messagesToSend = new LinkedList<String>();
    }

    public void addNextMessage(String message){
        synchronized (messagesToSend){
            hasMessages = true;
            messagesToSend.push(message);
        }
    }

    @Override
    public void run(){
        System.out.println("Welcome :" + userName);

        System.out.println("Local Port :" + socket.getLocalPort());
        System.out.println("Server = " + socket.getRemoteSocketAddress() + ":" + socket.getPort());

        try{
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), false);
            InputStream serverInStream = socket.getInputStream();
            Scanner serverIn = new Scanner(serverInStream);
            // BufferedReader userBr = new BufferedReader(new InputStreamReader(userInStream));
            // Scanner userIn = new Scanner(userInStream);

            while(!socket.isClosed()){
                if(serverInStream.available() > 0){
                    if(serverIn.hasNextLine()){
                        System.out.println(serverIn.nextLine());
                    }
                }
                if(hasMessages){
                    String nextSend = "";
                    synchronized(messagesToSend){
                        nextSend = messagesToSend.pop();
                        hasMessages = !messagesToSend.isEmpty();
                    }
                    serverOut.println(userName + " > " + nextSend);
                    serverOut.flush();
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
        }

    }
}
