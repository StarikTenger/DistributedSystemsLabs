import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.tools.json.JSONReader;
import com.rabbitmq.tools.json.JSONWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

public class Board {
    enum Directions {
        UL, U, UR,
        L, C, R,
        DL, D, DR
    }
    private static final String EXCHANGE_NAME_CALCULATED = "e_calculated_";
    private static final String EXCHANGE_NAME_UPDATED = "e_updated_";
    private static final String QUEUE_NAME_CALCULATED = "q_calculated_";
    private static final String QUEUE_NAME_UPDATED = "q_updated_";
    static final int BOARD_SIZE = 5;
    static final int MARGIN_SIZE = 1; // margin of processing neighbors
    private int id;
    private String exchangeCalculatedName;
    private String exchangeUpdatedName;
    private static Channel channel;
    private CellState[][] cells;
    private CellState[][] nextCells;

    public Board(int i, Integer[] neighbors) throws IOException, TimeoutException {
        cells = new CellState[BOARD_SIZE*3][BOARD_SIZE*3];
        nextCells = new CellState[BOARD_SIZE][BOARD_SIZE];
        id = i;
        exchangeCalculatedName = EXCHANGE_NAME_CALCULATED + id;
        exchangeUpdatedName = EXCHANGE_NAME_UPDATED + id;
        connect(neighbors);
    }

    public void connect(Integer[] neighbors) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(exchangeCalculatedName, "fanout");
        channel.exchangeDeclare(exchangeUpdatedName, "fanout");

        connectToNeighbors(neighbors);
    }

    public void connectToNeighbors(Integer[] neighbors) throws IOException {
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null) {
                DeliverCallback deliverCalculatedCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    handleNeighborCalc(Integer.valueOf(message));
                };
                declareQueue(QUEUE_NAME_CALCULATED, EXCHANGE_NAME_CALCULATED, neighbors[i], deliverCalculatedCallback);

                DeliverCallback deliverUpdatedCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    // TODO: parse from string to int and CellState[][]
                    // TODO: implement the new type???
                    JSONReader reader = new JSONReader();
                    CellState[][] neighborState = (CellState[][]) reader.read(message);
                    handleNeighborTable(index, neighborState);
                };
                declareQueue(QUEUE_NAME_UPDATED, EXCHANGE_NAME_UPDATED, neighbors[i], deliverUpdatedCallback);
            }
        }
    }

    private void declareQueue(String qName, String eName, int idNeighbor, DeliverCallback deliverCallback) throws IOException {
        String queueName = qName + id + idNeighbor;
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, eName + idNeighbor, "");
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    private void publishCalculated() throws IOException {
        String message = String.valueOf(id);
        channel.basicPublish(exchangeCalculatedName, "", null, message.getBytes("UTF-8"));
    }

    private void publishUpdated() throws IOException {
        JSONWriter rabbitmqJson = new JSONWriter();
        // TODO: write id of the publisher and the new cells
        // But maybe I just can get it from the name of the queue? It will be easier
        String messageCells = rabbitmqJson.write(cells);
        channel.basicPublish(exchangeUpdatedName, "", null, messageCells.getBytes());
    }

    public void calculateAllStates() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                nextCells[i][j] = calculateNextState(i + BOARD_SIZE,j + BOARD_SIZE);
            }
        }
    }

    public void updateCells() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cells[i+BOARD_SIZE][j+BOARD_SIZE] = nextCells[i][j];
            }
        }
    }

    public void handleNeighborCalc(int index) {

    }

    public void handleNeighborTable(int index, CellState[][] neighborState) {

    }

    public CellState calculateNextState(int x, int y) {
        int nCount = 0;
        for (int i = x - MARGIN_SIZE; i <= x + MARGIN_SIZE; i++) {
            for (int j = y - MARGIN_SIZE; j <= y + MARGIN_SIZE; j++) {
                if (i != x && j != x && getCellState(i, j).isAlive) nCount++;
            }
        }

        if (nCount == 3) return new CellState(true);
        if (nCount == 2 && getCellState(x, y).isAlive) return new CellState(true);
        return new CellState(false);
    }

    public CellState getCellState(int x, int y) {
        // don't forget to check the borders
        // TODO: define default state
        return cells[x][y];
    }
}
