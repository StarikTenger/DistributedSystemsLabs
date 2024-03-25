import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.concurrent.TimeoutException;

public class Main {
	// Parameters
    public static void main(String[] args) throws IOException, TimeoutException {


		System.out.println("Number of Command Line Argument = "+args.length);
	
		for(int i = 0; i< args.length; i++) {
			System.out.println(String.format("Command Line Argument %d is '%s'", i, args[i]));
		}

		int id = 0;
		Integer[] nbrs = new Integer[]{};

		if (Integer.valueOf(args[0]) == 1) {
			System.out.println("arg 1");
			id = 1;
			nbrs = new Integer[] {
				null, null, null, 
				null,       2, 
				null, null, null};
		} else if (Integer.valueOf(args[0]) == 2) {
			System.out.println("arg 2");
			id = 2;
			nbrs = new Integer[] {
				null, null, null, 
				1,       	null, 
				null, null, null};
		} else {
			System.out.println("No proper argument specified");
			return;
		}

		Board board = new Board(id, nbrs);

		System.out.println("Board created. Press enter to connect");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();
		
        board.connectToNeighbors();

		board.start();
    }
}