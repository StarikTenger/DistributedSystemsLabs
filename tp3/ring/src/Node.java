import com.rabbitmq.client.*;

import static java.lang.Math.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Node {
    private String queueName;
    private int id;
    private Status status;
	private Channel channel_in;
	private Channel channel_out;
	private String nextNodeQueue;

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

		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			try {
				String message = new String(delivery.getBody(), "UTF-8");
				log(queueName + " Received: " + 	message);
				handleMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		try (Connection connection = factory.newConnection()) {

			channel_in = connection.createChannel();
			//this.channel = channel;
			
			channel_in.queueDeclare(queueName, false, false, false, null);
			channel_in.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
	
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void connect(String nextNodeQueue) {

		

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");

		try (Connection connection = factory.newConnection()) {
			channel_out = connection.createChannel();
			channel_out.queueDeclare(nextNodeQueue, false, false, false, null);
			
			log("Connected " + queueName + " to " + nextNodeQueue);
		} catch (Exception e) {
			log("Failed to connect");
			e.printStackTrace();
		}
	}


    private void handleMessage(String message) throws Exception {
        if (message.startsWith("ELECTION")) {
            // Handle election message
            ElectionMessage electionMessage = deserializeElectionMessage(message.substring(9));
            handleElectionMessage(electionMessage);
        } else {
            // Process regular message
            // For simplicity, let's assume processing a regular message involves printing it
            log("Processing Regular Message: " + message);

            // Forward the message to the next node in the ring
            forwardMessage(channel_out, nextNodeQueue, message);
        }
    }

    public void startElection() throws Exception {
        status = Status.ELECTION_IN_PROGRESS;
        ElectionMessage electionMessage = new ElectionMessage();
        electionMessage.senderId = id;
        electionMessage.maxId = id;

        // Send election message to the next node in the ring
        channel_out.basicPublish("", nextNodeQueue, null, serializeElectionMessage(electionMessage).getBytes("UTF-8"));

        log("Started Election");
    }

    private void handleElectionMessage(ElectionMessage electionMessage) throws Exception {
        log("Received Election Message from Node " + electionMessage.senderId +
                " with maxId " + electionMessage.maxId);

        // Compare maxId to the node's id
        if (electionMessage.maxId > id) {
            // Update maxId and forward the election message
            electionMessage.maxId = id;
            forwardElectionMessage(channel_out, nextNodeQueue, electionMessage);
        } else if (electionMessage.senderId != id) {
            // Forward the election message to the next node
            forwardElectionMessage(channel_out, nextNodeQueue, electionMessage);
        } else if (electionMessage.senderId == id && electionMessage.maxId == id) {
            // Node becomes the leader
            status = Status.LEADER;
            log("Elected as Leader");
        }
    }

    private void forwardElectionMessage(Channel channel, String nextNodeQueue, ElectionMessage electionMessage) throws Exception {
        // Forward the election message to the next node in the ring
        String serializedMessage = "ELECTION" + serializeElectionMessage(electionMessage);
        channel_out.basicPublish("", nextNodeQueue, null, serializedMessage.getBytes("UTF-8"));
        log("Forwarded Election Message to Node " + nextNodeQueue);
    }

    private void forwardMessage(Channel channel, String nextNodeQueue, String message) throws Exception {
        // Forward the message to the next node in the ring
        channel_out.basicPublish("", nextNodeQueue, null, message.getBytes("UTF-8"));
        log(queueName + " Forwarded: " + message);
    }

    private String serializeElectionMessage(ElectionMessage message) {
        return message.senderId + "," + message.maxId;
    }

    private ElectionMessage deserializeElectionMessage(String data) {
        String[] parts = data.split(",");
        ElectionMessage message = new ElectionMessage();
        message.senderId = Integer.parseInt(parts[0]);
        message.maxId = Integer.parseInt(parts[1]);
        return message;
    }
}
