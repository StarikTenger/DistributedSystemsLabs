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
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            resMes(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public void ping() {}
    public void pong() {}
    public void start() {}
    public void init_conn() {}
    public void ok_conn() {}

    public void sendMes(String message) {
        try {
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(" [x] Sent '" + message + "'");
    }

    public static void resMes(String message) {
        String[] expression = message.split(" ");
        switch(expression[0]) {
            case "ping":
                // code block
                break;
            case "pong":
                // code block
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