import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {

    public static final String IMAGE_TO_GUI = "dskfna";

    private Database database;
    private String host;
    private int port;
    private Thread thread;
    boolean shutdown;

    public Server(String host, int port, Database database) {
        this.database = database;
        this.host = host;
        this.port = port;
        this.thread = new Thread(this);
        this.shutdown = false;
    }

    public void start() {
        this.thread.start();
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        Socket clientSocket;

        try {
            serverSocket = new ServerSocket(this.port, 3, InetAddress.getByName(this.host));
            System.out.println("Server:: " + serverSocket.getInetAddress() + " (" + serverSocket.getLocalPort() + ")");
            while (!this.shutdown) {
                System.out.println("Server:: Waiting for client...");
                clientSocket = serverSocket.accept();
                System.out.println("Server:: Accepted new client");
                ClientSocket client = new ClientSocket(clientSocket, this.database);
                client.start();
                System.out.println("Server:: Started client socket thread");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
