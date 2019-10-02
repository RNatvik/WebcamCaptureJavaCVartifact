package Regulering;

import java.util.Timer;

public class Main {


    public static void main(String[] args) {
	// write your code here

        long controllerRate = 100;
        long dpRate = 40;

        StorageBox sb = new StorageBox();

        Timer ftimer = new Timer();

        // Create timer tasks
        Controller controller = new Controller(sb);
        DataInProduser dp = new DataInProduser(sb);


        ftimer.scheduleAtFixedRate(controller,10, controllerRate);
        ftimer.scheduleAtFixedRate(dp,0,dpRate);



    }
}
