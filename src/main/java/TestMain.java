public class TestMain {

    public static void main(String[] args) {
        byte test = (byte) (600 & 0xFF);
        int test2 = (test & 0xFF);
        System.out.println(String.format("0x%02X", test));
        System.out.println(test2);

    }
}
