public class Main {

    public static void main(String[] args) {
        ImageStorageBox storageBox = new ImageStorageBox();
        Camera camera = new Camera(1, storageBox);
        ImageProcessor consumer = new ImageProcessor(storageBox);

        System.out.println("Setup camera...");
        boolean cameraSetupSuccess = camera.setup();

        if (cameraSetupSuccess) {
            System.out.println("Success!");
        } else {
            System.out.println("Failure!");
        }

        consumer.setup();

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        camera.stop();
        consumer.stop();
    }
}
