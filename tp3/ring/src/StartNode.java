import java.util.Scanner;

public class StartNode {
	public static void main(String[] args) {
        // Start three nodes in the ring
        Node node = new Node(args[0]);
		node.connect(args[1]);

		Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            if (message.startsWith("s")) {
				try {
					node.startElection();
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
    }
}
