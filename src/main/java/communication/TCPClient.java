package communication;

import data.PidParameter;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

    public static void main(String[] args) {
        try {
            PidParameter pidParameter = new PidParameter(9,8,7, true);
            Socket socket = new Socket(InetAddress.getLocalHost(), 4567);
            System.out.println(socket.isConnected());
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);

            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String message = String.format("SET::%s::%s", TCPClientSocket.PID1, pidParameter.toJSON());
            System.out.println(message);
            printWriter.println(message);
            printWriter.flush();
            String response = bufferedReader.readLine();
            System.out.println(response);

            message = String.format("GET::%s", TCPClientSocket.PID1);
            System.out.println(message);
            printWriter.println(message);
            printWriter.flush();
            response = bufferedReader.readLine();
            System.out.println(response);


            message = String.format("QUIT::%s", TCPClientSocket.PID2);
            printWriter.println(message);
            printWriter.flush();

            socket.close();


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
