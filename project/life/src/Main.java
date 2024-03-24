import java.io.IOException;
import java.lang.reflect.Array;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println("Hello world!");

        Board board0 = new Board(0, new Integer[] {null, null, null, null, 1, null, null, null});
        Board board1 = new Board(1, new Integer[] {null, null, null, 0, null, null, null, null});
    }
}