import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.random;


public class Pingponger {
    private static final String EXCHANGE_NAME_HANDSHAKE = "handshake";
    private static final String EXCHANGE_NAME_PINGPONG = "pingpong";
    private static final String SEVERITY_PING = "ping";
    private static final String SEVERITY_PONG = "pong";
    private static int id;
    private static Channel channel;
    private static Status status;
	private static int message_count;

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
        while (status == Status.IDLE) {
            String message = scanner.nextLine();
            if (message.startsWith("s")) {
                sendStart();
                break;
            }
        }
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
            sendHandshake("init_conn " + id);
            System.out.println("Sent init_conn");
        } else {
            System.out.println("Already started");
        }
    }

    public static void resMesHandshake(String message) throws IOException {
        String[] expression = message.split(" ");
        int idSender = Integer.valueOf(expression[1]);
        if (idSender != id) {
            System.out.println("Received " + expression[0]);
            switch(expression[0]) {
                case "init_conn":
                    if (idSender < id) {
                        status = Status.STARTED;
                        sendHandshake("ok_conn " + id);
                        System.out.println("Sent ok_conn");
                        confPingPong(false);
                    } else if (status == Status.IDLE && idSender > id) {
                        status = Status.WAITING;
                        sendHandshake("init_conn " + id);
                        System.out.println("No, your id is bigger, sent ok_conn");
                    }
                    break;
                case "ok_conn":
                    status = Status.STARTED;
                    confPingPong(true);
                    break;
                default:
                    // code block
            }
        }
    }

    public static void confPingPong(Boolean isPing) throws IOException {
        String severitySub = isPing ? SEVERITY_PONG : SEVERITY_PING;
        String severityPub = isPing ? SEVERITY_PING : SEVERITY_PONG;
        String mes = isPing ? "ping" : "pong";

        channel.exchangeDeclare(EXCHANGE_NAME_PINGPONG, "direct");
        String queueName = channel.queueDeclare().getQueue();
        // subscribe to messages
        channel.queueBind(queueName, EXCHANGE_NAME_PINGPONG, severitySub);

        // send the first pong
        if (isPing) sendPingPong(mes, severityPub);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
			message_count++;
            System.out.println(String.valueOf(message_count) + ") Received " + message + " time: " + time);
            sendPingPong(mes, severityPub);
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }


    public static void sendHandshake(String message) {
        try {
            channel.basicPublish(EXCHANGE_NAME_HANDSHAKE, "", null, message.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void sendPingPong(String message, String severity) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            channel.basicPublish(EXCHANGE_NAME_PINGPONG, severity, null, message.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}