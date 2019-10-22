import data.Flag;
import image_processing.Camera;
import image_processing.ImageProcessor;
import pub_sub_service.Broker;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestMain {

    public static void main(String[] args) {
        Broker broker = new Broker();
        Flag flag = new Flag(false);
        Camera cam = new Camera(0, flag);
        ImageProcessor processor = new ImageProcessor(flag, broker);

        cam.start();
        processor.start(cam.getSrcIm());

        ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);
        ses.scheduleAtFixedRate(cam, 0, 40, TimeUnit.MILLISECONDS);
        ses.scheduleAtFixedRate(processor, 1000, 10, TimeUnit.MILLISECONDS);

        try {
            Thread.sleep(90000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cam.stop();
        processor.stop();
        ses.shutdown();
        try {
            ses.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
