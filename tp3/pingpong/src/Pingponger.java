import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.random;


public class Pingponger {
    private static final String EXCHANGE_NAME_HANDSHAKE = "handshake";
    private static final String SEVERITY = "pingpong";
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

        connect();
        handshake();

        System.out.println("Send s to start");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            if (message.startsWith("s")) {
                sendStart();
            }
        }

//        sendPing();
    }

    private static void connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        System.out.println("Connected");
        System.out.println("ID of participant '" + id + "'");
    }

    private static void handshake() throws IOException, TimeoutException {
        channel.exchangeDeclare(EXCHANGE_NAME_HANDSHAKE, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME_HANDSHAKE, "");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            resMesHandshake(message);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public static void sendStart() {
        if (status == Status.IDLE) {
            status = Status.WAITING;
            sendMes("init_conn " + id);
            System.out.println("Sent init_conn");
        } else {
            System.out.println("Already started");
        }
    }

    public static void resMesHandshake(String message) {
        String[] expression = message.split(" ");
        int idSender = Integer.valueOf(expression[1]);
        if (idSender != id) {
            switch(expression[0]) {
                case "init_conn":
                    System.out.println("Received init_conn");
                    if (idSender < id) {
                        status = Status.STARTED;
                        sendMes("ok_conn " + id);
                        System.out.println("Sent ok_conn");
                    } else if (status == Status.IDLE && idSender > id) {
                        status = Status.WAITING;
                        sendMes("init_conn " + id);
                        System.out.println("No, your id is bigger, sent ok_conn");
                    }
                    break;
                case "ok_conn":
                    status = Status.STARTED;
                    System.out.println("Received ok_conn");
//                    sendPing();
                    break;
                default:
                    // code block
            }
        }
    }

    public static void sendPing() {
        sendMes("ping " + id);
    }
    public static void sendPong() {
        sendMes("pong " + id);
    }

    public static void sendMes(String message) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            channel.basicPublish(EXCHANGE_NAME_HANDSHAKE, "", null, message.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void resMes(String message) {
        String[] expression = message.split(" ");
        switch(expression[0]) {
            case "ping":
                System.out.println("Received ping from: " + expression[1]);
                sendPong();
                break;
            case "pong":
                System.out.println("Received pong from: " + expression[1]);
                sendPing();
                break;
            default:
                // code block
                break;
        }
    }
}