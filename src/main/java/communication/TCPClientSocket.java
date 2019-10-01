package communication;

import data.DataStorage;

import java.io.*;
import java.net.Socket;

public class TCPClientSocket implements Runnable {

    private Socket socket;
    private DataStorage dataStorage;
    private InputStream inputStream;
    private OutputStream outputStream;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;


    public TCPClientSocket(Socket socket, DataStorage dataStorage) {
        this.socket = socket;
        try {
            this.socket.setKeepAlive(true);
            this.dataStorage = dataStorage;
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
            this.printWriter = new PrintWriter(this.outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create TCP client socket");
        }
    }

    @Override
    public void run() {
        try {
            boolean shutdown = false;
            while (!shutdown) {
                String messageIn = this.bufferedReader.readLine();
                System.out.println("Client wrote: " + messageIn);
                if (messageIn.equals("GET:DATA")) {
                    this.printWriter.println(this.dataStorage.getData());
                } else if (messageIn.equals("QUIT")) {
                    shutdown = true;
                    this.printWriter.println("Goodbye");
                } else {
                    this.printWriter.println(messageIn);
                }
                this.printWriter.flush();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.socket.close();
            System.out.println("Socket closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
