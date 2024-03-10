import com.rabbitmq.client.*;

import static java.lang.Math.random;
import java.text.SimpleDateFormat;
import java.util.*;

public class Node {
    private String queueName;
	private int id;

	private void log(String str) {
		String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		System.out.println("[Node " + String.valueOf(id) + " at " + time + "] " + str);
	}

    public Node(String queueName) {
		id = (int) (random()*1000);
        this.queueName = queueName;
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

                //Thread.sleep(1000); // Add some delay between iterations
            }
        }
    }

    private void forwardMessage(Channel channel, String nextNodeQueue, String message) throws Exception {
        // Forward the message to the next node in the ring
        channel.basicPublish("", nextNodeQueue, null, message.getBytes("UTF-8"));
        System.out.println(queueName + " Forwarded: " + message);
    }
}
