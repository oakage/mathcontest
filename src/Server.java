import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server
{
    private static final int port = 4444;

    private int serverPort;
    private List<ReadThread> clients;
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
        MulticastSocket serverSocket = null;
        try {
            serverSocket = new MulticastSocket(serverPort);
            acceptClients(serverSocket);
        } catch (IOException e){
            System.err.println("Could not listen on port: "+serverPort);
            System.exit(1);
        }
    }

    private void acceptClients(MulticastSocket serverSocket){

        int ip = 0;

        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            String ips = socket.getLocalAddress().getHostAddress();
            ip = Integer.parseInt(ips);
        }
        catch (SocketException e)
        {
            System.out.print(e.getMessage());
        }
        catch (UnknownHostException e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println("server starts port = " + serverSocket.getLocalSocketAddress());
        while(true){
            try{
                Socket socket = serverSocket.accept();
                System.out.println("accepts : " + socket.getRemoteSocketAddress());
                ReadThread client = new ReadThread(socket, this, ip);
                Thread thread = new Thread(client);
                thread.start();
                clients.add(client);
            } catch (IOException ex){
                System.out.println("Accept failed on : "+serverPort);
            }
        }
    }

}
