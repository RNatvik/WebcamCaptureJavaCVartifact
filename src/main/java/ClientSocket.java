import data.Image;
import org.bytedeco.javacpp.indexer.UByteBufferIndexer;
import org.bytedeco.javacv.Frame;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

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
        BufferedReader bufferedReader;

        OutputStream outputStream;
        PrintWriter printWriter;
        try {
            System.out.println("ClientSocket:: Initializing streams");
            inputStream = this.socket.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            outputStream = this.socket.getOutputStream();
            printWriter = new PrintWriter(outputStream);

            System.out.println("ClientSocket:: Starting comLoop");
            comLoop:
            while (!this.shutdown) {
                System.out.println("ClientSocket:: Waiting for line...");
                String line = bufferedReader.readLine();
                System.out.println("ClientSocket:: got line: " + line);
                String[] message = line.split(",");
                switch (message[0]) {
                    case "GET":
                        this.getCase(message[1], printWriter);
                        break;

                    case "SET":
                        break;

                    case "QUIT":
                        break comLoop;

                    default:
                        break;
                }
            }

            this.socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCase(String varName, PrintWriter printWriter) {
        switch (varName) {
            case Server.IMAGE_TO_GUI:
                System.out.println("ClientSocket:: getting image and variables");
                Image image = this.database.getImageToGUI();
                Frame frame = image.getFrame();
                UByteBufferIndexer indexer = frame.createIndexer();

                HashMap<String, int[]> map = new HashMap<>();
                map.put("info", new int[]{frame.imageWidth, frame.imageHeight, frame.imageChannels});

                System.out.println("ClientSocket:: Mapping values");
                for (int x = 0; x < frame.imageWidth; x++) {
                    for (int y = 0; y < frame.imageHeight; y++) {
                        String key = "" + x + "," + y;
                        int b = indexer.get(x,y,0);
                        int g = indexer.get(x,y,1);
                        int r = indexer.get(x,y,2);
                        map.put(key, new int[]{b,g,r});
                    }
                }
                System.out.println("ClientSocket:: Creating JSON object");
                JSONObject jsonObject = new JSONObject(map);
                System.out.println("ClientSocket:: Writing object");
                printWriter.write(jsonObject.toString());
                printWriter.flush();
                break;

            default:
                break;
        }
    }
}
