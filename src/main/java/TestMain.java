import data.Data;
import data.Image;
import org.bytedeco.javacv.Frame;

public class TestMain {

    public static void main(String[] args) {
        Data data = new Image(new Frame());
        Class cls = data.getClass();
        System.out.println(cls);
        if (cls.toString().equals("class data.Image")) {
            System.out.println("true");
        }

    }
}
