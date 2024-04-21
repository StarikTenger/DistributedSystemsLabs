import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.concurrent.TimeoutException;
import java.io.FileReader;

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

	public static Boolean waitInput() throws IOException {
		char c = (char) System.in.read();
		return c == 'q';
	}

	public static Board loadBoard(String filename, int id, Integer[] nbrs) throws IOException, TimeoutException, InterruptedException {	
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read the size of the matrix
            int size = Integer.parseInt(br.readLine());
			Board board = new Board(size, id, nbrs);

            // Read and fill the matrix
            for (int i = 0; i < size; i++) {
                String[] row = br.readLine().split(" ");
                for (int j = 0; j < size; j++) {
                    int val = Integer.parseInt(row[j]);
                    board.setCell(j, i, new CellState(val == 1));
                }
            }
			return board;
        } catch (IOException e) {
			System.out.println("failed to open file " + filename);
			System.out.println("Terminating...");
			System.exit(0);
			return null;
        }
    }

	// Parameters
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {


		if (args.length != 4) {
			System.out.println("Invalid number of arguments. \nUsage ./program n m id board_file");
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
		Board board = loadBoard(args[3], id, nbrs);

		System.out.println("Board created. Press enter to connect");
		if (waitInput()) {
			System.out.println("Terminating...");
			System.exit(0);
		}

		if (!board.connectToNeighbors()) {
			System.out.println("Terminating due to failure...");
			System.exit(0);
			return;
		}

		System.out.println("Board connected. Press enter to start");
		if (waitInput()) {
			System.out.println("Terminating...");
			System.exit(0);
		}
		
		int cycles = 10;

		while (true) {
			board.start(cycles);
			System.out.println("Press enter to run " + String.valueOf(cycles) + " more cycles, press q to quit");
			if (waitInput()) {
				break;
			}
		}

		System.out.println("Terminating...");
		System.exit(0);
    }
}