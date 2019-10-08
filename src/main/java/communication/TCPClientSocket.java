package communication;

import data.Circle;
import data.Data;
import data.DataStorage;
import data.PidParameter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClientSocket implements Runnable {
    public static final String CIRCLE = "CIRCLE";
    public static final String PID1 = "PID1";
    public static final String PID2 = "PID2";
    public static final String ERROR = "ERROR";

    private Socket socket;
    private DataStorage dataStorage;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private boolean shutdown;
    private boolean terminated;


    public TCPClientSocket(Socket socket, DataStorage dataStorage) {
        this.socket = socket;
        try {
            this.dataStorage = dataStorage;
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.printWriter = new PrintWriter(this.socket.getOutputStream());
            this.shutdown = false;
            this.terminated = false;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not create TCP client socket");
        }
    }

    public void stop() {
        this.shutdown = true;
    }

    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void run() {
        while (!this.shutdown) {
            try {
                String response;
                String request = this.bufferedReader.readLine();
                System.out.println(request);
                String[] command = request.split("::");
                if (command[0].equals("GET")) {
                    response = get(command[1]);
                } else if (command[0].equals("SET")) {
                    System.out.println("Found SET");
                    response = set(command[1], command[2]);
                } else if (command[0].equals("QUIT")) {
                    this.shutdown = true;
                    response = "OK";
                } else {
                    response = ERROR;
                }
                this.printWriter.println(response);
                this.printWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.socket.close();
            System.out.println("Socket closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String get(String variable) {
        String result;

        switch (variable) {
            case CIRCLE:
                result = this.dataStorage.getCircle().toJSON();
                break;

            case PID1:
                result = this.dataStorage.getPidParameter1().toJSON();
                break;

            case PID2:
                result = this.dataStorage.getPidParameter2().toJSON();
                break;

            default:
                result = String.format("\"type\":\"%s\"", ERROR);
        }
        return result;
    }

    private String set(String variable, String json) {
        String reponse = ERROR;
        JSONObject jobj = new JSONObject(json);

        switch (variable) {
            case PID1:
                if (jobj.get("type").equals(Data.PID_PARAM)) {
                    PidParameter pid = new PidParameter(
                            jobj.getInt("kp"),
                            jobj.getInt("ki"),
                            jobj.getInt("kd"),
                            jobj.getBoolean("flag")
                    );
                    this.dataStorage.setPidParameter1(pid);
                    reponse = "OK";
                }
                break;

            case PID2:
                if (jobj.get("type").equals(Data.PID_PARAM)) {
                    PidParameter pid = new PidParameter(
                            jobj.getInt("kp"),
                            jobj.getInt("ki"),
                            jobj.getInt("kd"),
                            jobj.getBoolean("flag")
                    );
                    this.dataStorage.setPidParameter2(pid);
                    reponse = "OK";
                }
                break;

            case CIRCLE:
                if (jobj.get("type").equals(Data.CIRCLE)) {
                    JSONArray array = jobj.getJSONArray("location");
                    Circle circle = new Circle(
                            new int[] {
                                    array.getInt(0),
                                    array.getInt(1),
                                    array.getInt(2)
                            },
                            jobj.getBoolean("flag")
                    );
                    this.dataStorage.setCircle(circle);
                    reponse = "OK";
                }
                break;

            default:
                break;
        }
        return reponse;
    }
}
