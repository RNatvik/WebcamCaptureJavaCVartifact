package communication;

import data.Topic;
import org.json.JSONException;
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

    private String outputMessage;

    public TCPClient(InetAddress hostAddress, int hostPort) {
        try {
            this.thread = new Thread(this);
            this.socket = new Socket(hostAddress, hostPort);
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.shutdown = false;
            this.terminated = false;
            this.socket.setSoTimeout(5);
            this.outputMessage = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TCPClient(String hostAddress, int hostPort) {
        try {
            this.thread = new Thread(this);
            this.socket = new Socket(hostAddress, hostPort);
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.shutdown = false;
            this.terminated = false;
            this.socket.setSoTimeout(5);
            this.outputMessage = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startThread() {
        System.out.println(this + ":: starting thread");
        this.thread.start();
    }

    public void stop() {
        System.out.println(this + ":: stop() called");
        this.shutdown = true;
    }

    public boolean isTerminated() {
        return this.terminated;
    }

    public void setOutputMessage(String command, String body) {
        this.outputMessage = String.format(command+"::%s", body);
        System.out.println(this + ":: set output message: " + this.outputMessage);
    }

    @Override
    public void run() {
        this.printWriter.println(String.format("SUB::%s", Topic.IMAGE_DATA));
        this.printWriter.flush();
        while (!this.shutdown) {
            try {
                if (this.outputMessage != null) {
                    this.printWriter.println(this.outputMessage);
                    this.printWriter.flush();
                    this.outputMessage = null;
                }
                String response = this.bufferedReader.readLine();
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject data = jsonObject.getJSONObject("data");
                        String location = data.getJSONArray("location").toString();
                        System.out.println(this + ":: " + location);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        System.out.println(this + ":: Error when parsing JSON object.\n   Response: " + response);
                    }
                } else {
                    System.out.println(this + ":: Socket closed remotely");
                    this.stop();
                }
            } catch (SocketTimeoutException e) {

            } catch (IOException e) {
                e.printStackTrace();
                this.stop();
            }
        }
        this.terminated = this.shutdownProcedure();
        System.out.println(this + ":: terminated: " + this.terminated);
    }

    private boolean shutdownProcedure() {
        boolean success = true;
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }
}
