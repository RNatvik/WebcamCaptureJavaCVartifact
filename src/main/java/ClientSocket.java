import data.Image;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.opencv_core.FileStorage;
import org.bytedeco.opencv.opencv_core.IplImage;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ClientSocket implements Runnable {

    private Database database;
    private Thread thread;
    private Socket socket;
    private boolean shutdown;

    public ClientSocket(Socket socket, Database database) {
        this.database = database;
        this.socket = socket;
        this.thread = new Thread(this);
        this.shutdown = false;
    }

    public void start() {
        this.thread.start();
    }

    @Override
    public void run() {
        InputStream inputStream;
        ObjectInputStream objectInputStream;
        BufferedReader bufferedReader;

        OutputStream outputStream;
        ObjectOutputStream objectOutputStream;
        try {
            inputStream = this.socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            outputStream = this.socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);

            comLoop:
            while (!this.shutdown) {
                String line = bufferedReader.readLine();
                String[] message = line.split(",");
                switch (message[0]) {
                    case "GET":

                        break;

                    case "SET":
                        break;

                    case "QUIT":
                        break;

                    default:
                        break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCase(String varName) {
        switch (varName) {
            case Server.IMAGE_TO_GUI:
                Image image = this.database.getImageToGUI();
                break;

            default:
                break;
        }
    }
}
