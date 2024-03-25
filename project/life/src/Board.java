import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.tools.json.JSONReader;
import com.rabbitmq.tools.json.JSONWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Board {
    enum Directions {
        UL, U, UR,
        L,     R,
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
	private Boolean running = true;
	private Integer[] neighbors;
	private Boolean[] neighborsCalculated;
	private Boolean[] neighborsUpdated;
        
    public Board(int i, Integer[] _neighbors) throws IOException, TimeoutException {
		neighbors = _neighbors;
        cells = new CellState[BOARD_SIZE * 3][BOARD_SIZE * 3];
        nextCells = new CellState[BOARD_SIZE][BOARD_SIZE];
		neighborsCalculated = new Boolean[8];
		neighborsUpdated = new Boolean[8];
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

	public void start() throws IOException {
		while (running) {
			// Flush updated neighbors
			Arrays.fill(neighborsUpdated, false);

			// Notify neighbors that I am updated
			publishUpdated();

			// Wait for states of neighbors to be updated
			while(!allNeighborsUpdated()) {} // TODO: get rid of busy waiting

			calculateAllStates();

			// Flush updated neighbors
			Arrays.fill(neighborsCalculated, false);

			// Notify neighbors that I am calculated
			publishCalculated();

			// Wait for all neighbors to be calculated
			while(!allNeighborsCalculated()) {} // TODO: get rid of busy waiting

			updateCells();
		}
	}

	private Boolean allNeighborsCalculated() {
		for (int i = 0; i < 8; i++) {
			if (neighbors[i] != null && !neighborsCalculated[i]) return false;
		}
		return true;
	}

	private Boolean allNeighborsUpdated() {
		for (int i = 0; i < 8; i++) {
			if (neighbors[i] != null && !neighborsUpdated[i]) return false;
		}
		return true;
	}

    private void calculateAllStates() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                nextCells[i][j] = calculateNextState(i + BOARD_SIZE,j + BOARD_SIZE);
            }
        }
    }
	
    private void updateCells() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cells[i+BOARD_SIZE][j+BOARD_SIZE] = nextCells[i][j];
            }
        }
    }

	public void handleNeighborCalc(int index) {

    }

	private Vec2i getVectorForDirection(Directions direction) {
        switch (direction) {
            case UL: return new Vec2i(-1, -1);   // Up Left
            case U:  return new Vec2i(0, -1);  // Up
            case UR: return new Vec2i(1, -1);  // Up Right
            case L:  return new Vec2i(-1, 0);  // Left
            case R:  return new Vec2i(1, 0); // Right
            case DL: return new Vec2i(-1, 1);  // Down Left
            case D:  return new Vec2i(0, 1); // Down
            case DR: return new Vec2i(1, 1); // Down Right
            default: throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }


    private void handleNeighborTable(int index, CellState[][] neighborCells) {
		// Coordinates of topleft corner on united board
		Vec2i delta = getVectorForDirection(Directions.values()[index]).add(new Vec2i(1,1)).mult(BOARD_SIZE);

		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				cells[delta.x + i][delta.y + j] = neighborCells[i][j];
			}	
		}

		neighborsCalculated[index] = true;
    }


    private CellState calculateNextState(int x, int y) {
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

    private CellState getCellState(int x, int y) {
        // don't forget to check the borders
        // TODO: define default state
        return cells[x][y];
    }
}
