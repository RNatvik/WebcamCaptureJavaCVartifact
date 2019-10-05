import communication.UDPServer;
import data.*;
import image_processing.Camera;
import image_processing.ImageProcessor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
        Image image = new Image(false);
        Circle circle = new Circle(new int[]{0,0,0},false);
        PidParameter pid1 = new PidParameter(1,1,1,false);
        PidParameter pid2 = new PidParameter(1,1,1,false);
        DataStorage storage = new DataStorage(image, circle, pid1, pid2);

        Flag imFlag = new Flag(false);
        Camera camera = new Camera(0, imFlag);
        ImageProcessor processor = new ImageProcessor(imFlag, image, circle);
        UDPServer udpServer = new UDPServer(2345, storage, true, 3);

        try {
            camera.start();
            processor.start(camera.getSrcIm());
            ses.scheduleAtFixedRate(camera, 0, 40, TimeUnit.MILLISECONDS);
            ses.schedule(processor, 1, TimeUnit.SECONDS);

            udpServer.startThread();

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        udpServer.stop();
        processor.stop();
        camera.stop();


        boolean finished = false;
        while (!finished) {
            System.out.print("");
            if (camera.isTerminated() && processor.isTerminated() && udpServer.isTerminated()) {
                System.out.println("process should terminate");
                finished = true;
            }
        }
        ses.shutdown();
    }
}
