package communication;

import data.DataStorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClientSocket implements Runnable {

    private Socket socket;
    private DataStorage dataStorage;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;


    public TCPClientSocket(Socket socket, DataStorage dataStorage) {
        this.socket = socket;
        try {
            this.socket.setKeepAlive(true);
            this.dataStorage = dataStorage;
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create TCP client socket");
        }
    }

    @Override
    public void run() {


        try {
            this.socket.close();
            System.out.println("Socket closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
