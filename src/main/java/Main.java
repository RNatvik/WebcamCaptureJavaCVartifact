import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(5);
        Flag imFlag = new Flag(false);
        Camera camera = new Camera(0, imFlag);
        ImageProcessor processor = new ImageProcessor(imFlag);

        try {
            camera.start();
            processor.start(camera.getSrcIm());
            ses.scheduleAtFixedRate(camera, 0, 40, TimeUnit.MILLISECONDS);
            ses.schedule(processor, 1, TimeUnit.SECONDS);

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        processor.stop();
        camera.stop();


        boolean finished = false;
        while (!finished) {
            System.out.print("");
            if (camera.isTerminated() && processor.isTerminated()) {
                System.out.println("process should terminate");
                finished = true;
            }
        }
        ses.shutdown();
    }
}
