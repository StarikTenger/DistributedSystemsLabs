public class RingTopology {
    public static void main(String[] args) {
        // Start three nodes in the ring
        Node node1 = new Node("node1-queue", "node2-queue");
        Node node2 = new Node("node2-queue", "node3-queue");
        Node node3 = new Node("node3-queue", "node1-queue");


        // Connect nodes in the ring
        try {
            node1.connect();
            node2.connect();
            node3.connect();
			Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

		try {
            node1.startElection();
        } catch (Exception e) {
            e.printStackTrace();
        }


		while(true) {}
    }
}
