package communication;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(InetAddress.getLocalHost(), 12345);
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);

            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            System.out.println("TCPClient:: printWriter writing");
            printWriter.println("GET:DATA");
            printWriter.flush();
            String line = bufferedReader.readLine();
            System.out.println("TCPClient:: Server responded: " + line);
            printWriter.println("Does this echo?");
            printWriter.flush();
            line = bufferedReader.readLine();
            System.out.println("TCPClient:: Server responded: " + line);
            printWriter.println("QUIT");
            printWriter.flush();
            line = bufferedReader.readLine();
            System.out.println("TCPClient:: Server responded: " + line);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
