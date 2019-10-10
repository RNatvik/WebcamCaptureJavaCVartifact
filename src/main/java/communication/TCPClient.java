package communication;

import data.Topic;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(InetAddress.getLoopbackAddress(), 1234);
            System.out.println(socket.isConnected());
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);

            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String message = String.format("SUB::%s", Topic.IMAGE_DATA);
            printWriter.println(message);
            printWriter.flush();

            for (int i = 0; i < 10; i++) {
                String response = bufferedReader.readLine();
                System.out.println(response);
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
