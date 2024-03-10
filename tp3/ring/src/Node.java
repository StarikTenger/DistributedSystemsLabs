import com.rabbitmq.client.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Node {
    private String queueName;
    private int id;
    private Status status;

    enum Status {
        IDLE,
        STARTED,
        LEADER,
        NOT_LEADER,
        ELECTION_IN_PROGRESS
    }

    private class ElectionMessage {
        public int senderId;
        public int maxId;
    }

    private void log(String str) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.println("[Node " + String.valueOf(id) + " at " + time + "] " + str);
    }

    public Node(String queueName) {
        id = (int) (Math.random() * 1000);
        this.queueName = queueName;
        status = Status.IDLE;
        log("Created");
    }

    public void connect(String nextNodeQueue) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare(queueName, false, false, false, null);

            while (true) {
                // Receive a message
                GetResponse response = channel.basicGet(queueName, true);
                if (response != null) {
                    String message = new String(response.getBody(), "UTF-8");
                    System.out.println(queueName + " Received: " + message);

                    // Process the message (e.g., perform some task)

                    // Forward the message to the next node in the ring
                    forwardMessage(channel, nextNodeQueue, message);
                }

                // Check if an election needs to be started
                if (status == Status.IDLE) {
                    startElection(channel, nextNodeQueue);
                }

                //Thread.sleep(1000); // Add some delay between iterations
            }
        }
    }

    private void forwardMessage(Channel channel, String nextNodeQueue, String message) throws Exception {
        // Forward the message to the next node in the ring
        channel.basicPublish("", nextNodeQueue, null, message.getBytes("UTF-8"));
        System.out.println(queueName + " Forwarded: " + message);
    }

    private void startElection(Channel channel, String nextNodeQueue) throws Exception {
        status = Status.ELECTION_IN_PROGRESS;
        ElectionMessage electionMessage = new ElectionMessage();
        electionMessage.senderId = id;
        electionMessage.maxId = id;

        // Send election message to the next node in the ring
        channel.basicPublish("", nextNodeQueue, null, serializeElectionMessage(electionMessage));

        log("Started Election");

        // Wait for a response from the next node
        TimeUnit.SECONDS.sleep(2); // Adjust the waiting time based on your requirements

        // Check if this node won the election
        if (status == Status.ELECTION_IN_PROGRESS) {
            status = Status.LEADER;
            log("Elected as Leader");
        }
    }

    private byte[] serializeElectionMessage(ElectionMessage message) throws Exception {
        // Convert ElectionMessage to byte array (You may use serialization libraries like Gson, Jackson, etc.)
        // For simplicity, we use a simple string format
        return (message.senderId + "," + message.maxId).getBytes("UTF-8");
    }

    private ElectionMessage deserializeElectionMessage(byte[] data) throws Exception {
        // Convert byte array to ElectionMessage (You may use serialization libraries like Gson, Jackson, etc.)
        // For simplicity, we use a simple string format
        String[] parts = new String(data, "UTF-8").split(",");
        ElectionMessage message = new ElectionMessage();
        message.senderId = Integer.parseInt(parts[0]);
        message.maxId = Integer.parseInt(parts[1]);
        return message;
    }
}
