package communication;

import data.Topic;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient implements Runnable {

    private Thread thread;
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private boolean shutdown;
    private boolean terminated;

    private String ouputMessage;

    public TCPClient(InetAddress hostAddress, int hostPort) {
        try {
            this.thread = new Thread(this);
            this.socket = new Socket(hostAddress, hostPort);
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.shutdown = false;
            this.terminated = false;
            this.socket.setSoTimeout(5);
            this.ouputMessage = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startThread() {

        this.thread.start();
    }

    public void stop() {
        this.shutdown = true;
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    public void setOutputMessage(String command, String body) {
        this.ouputMessage = String.format(command+"::%s", body);
    }

    @Override
    public void run() {
        this.printWriter.println(String.format("SUB::%s", Topic.IMAGE_DATA));
        this.printWriter.flush();
        while (!this.shutdown) {
            try {
                if (this.ouputMessage != null) {
                    this.printWriter.println(this.ouputMessage);
                    this.printWriter.flush();
                    this.ouputMessage = null;
                }
                String response = this.bufferedReader.readLine();
                if (response != null) {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject data = jsonObject.getJSONObject("data");
                    String location = data.getJSONArray("location").toString();
                    System.out.println(location);
                } else {
                    System.out.println("TCPClient:: Socket closed remotely");
                    this.stop();
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
                this.stop();
            }
        }
        this.terminated = this.shutdownProcedure();
    }

    private boolean shutdownProcedure() {
        System.out.println("TCPClient:: in shutdown procedure");
        boolean success = true;
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }
}
