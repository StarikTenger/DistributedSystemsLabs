import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.concurrent.TimeoutException;

public class Main {
	public static int id;
	public static int n;
	public static int m;

	public static Integer check_neighbor(Board.Directions dir) {
		Vec2i pos = new Vec2i((id - 1) % n, (id - 1) / n);

		pos = pos.add(Board.getVectorForDirection(dir));

		if (pos.x < 0 || pos.x >= n || pos.y < 0 || pos.y >= m) {
			return null;
		}

		return pos.y * n + pos.x + 1;
	}

	// Parameters
    public static void main(String[] args) throws IOException, TimeoutException {


		if (args.length != 3) {
			System.out.println("Invalid number of arguments. \nUsage ./program n m id");
			return;
		}

		
		n = Integer.valueOf(args[0]);
		m = Integer.valueOf(args[1]);
		id = Integer.valueOf(args[2]);

		if (n <= 0 || m <= 0 || id <= 0 || id > n * m) {
			System.out.println("Invalid arguments:");
			System.out.println("n and m must be > 0");
			System.out.println("id must be between 1 and n * m");
			return;
		}

		Integer[] nbrs = new Integer[]{};
		nbrs = new Integer[] {
			check_neighbor(Board.Directions.UL), 
			check_neighbor(Board.Directions.U), 
			check_neighbor(Board.Directions.UR),
			check_neighbor(Board.Directions.L),
			check_neighbor(Board.Directions.R),
			check_neighbor(Board.Directions.DL), 
			check_neighbor(Board.Directions.D), 
			check_neighbor(Board.Directions.DR)
		};
		Board board = new Board(id, nbrs);

		System.out.println("Board created. Press enter to connect");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();

		board.connectToNeighbors();

		System.out.println("Board connected. Press enter to start");
		br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();

		
		board.start();
    }
}