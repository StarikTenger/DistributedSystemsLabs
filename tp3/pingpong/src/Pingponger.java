import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;

import static java.lang.Math.random;


public class Pingponger {
    private static final String EXCHANGE_NAME = "ping";
    private static int id;
    private static Channel channel;
    private static Status status;

    enum Status {
        IDLE,
        WAITING,
        STARTED
    }

    public static void main(String[] argv) throws Exception {
        // signals: ping, pong, start, init_conn, ok_conn
        // statuses: idle, waiting, started
        id = (int) (random()*1000);
        status = Status.IDLE;


        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        System.out.println(" ID of participant '" + id + "'");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            resMes(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        sendPing();
    }

    public static void sendPing() {
        sendMes("ping " + id);
    }
    public static void sendPong() {
        sendMes("pong " + id);
    }
    public void start() {}
    public void init_conn() {}
    public void ok_conn() {}

    public static void sendMes(String message) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resMes(String message) {
        String[] expression = message.split(" ");
        if (Integer.valueOf(expression[1]) != id) {
            switch(expression[0]) {
                case "ping":
                    System.out.println("Received ping from: " + expression[1]);
                    sendPong();
                    break;
                case "pong":
                    System.out.println("Received pong from: " + expression[1]);
                    sendPing();
                    break;
                case "start":
                    // code block
                    break;
                case "init_conn":
                    // code block
                    break;
                case "ok_conn":
                    // code block
                    break;
                default:
                    // code block
            }
        }
    }
}