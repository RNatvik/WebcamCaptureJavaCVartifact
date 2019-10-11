import communication.TCPServer;
import communication.UDPServer;
import data.Flag;
import image_processing.Camera;
import image_processing.ImageProcessor;
import pub_sub_service.Broker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMain {

    public static void main(String[] args) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
        Broker broker = new Broker();

        Flag imFlag = new Flag(false);
        Camera camera = new Camera(0, imFlag);
        ImageProcessor processor = new ImageProcessor(imFlag, broker);
        TCPServer tcpServer = new TCPServer(1234, true, 3, broker);
        UDPServer udpServer = new UDPServer(2345, true, 3, broker);

        try {
            camera.start();
            processor.start(camera.getSrcIm());
            ses.scheduleAtFixedRate(camera, 0, 40, TimeUnit.MILLISECONDS);
            ses.scheduleAtFixedRate(broker, 0, 5, TimeUnit.MILLISECONDS);
            ses.schedule(processor, 1, TimeUnit.SECONDS);
            ses.schedule(tcpServer, 0, TimeUnit.SECONDS);
            ses.schedule(udpServer, 0, TimeUnit.SECONDS);

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        processor.stop();
        camera.stop();
        tcpServer.stop();
        udpServer.stop();


        boolean finished = false;
        while (!finished) {
            System.out.print("");
            if (camera.isTerminated() && processor.isTerminated() && tcpServer.isTerminated() && udpServer.isTerminated()) {
                System.out.println("process should terminate");
                finished = true;
            }
        }
        ses.shutdown();

    }


}
