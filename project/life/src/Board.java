import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.tools.json.JSONReader;
import com.rabbitmq.tools.json.JSONWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        
    public Board(int _id, Integer[] _neighbors) throws IOException, TimeoutException {
        cells = new CellState[BOARD_SIZE * 3][BOARD_SIZE * 3];
        nextCells = new CellState[BOARD_SIZE][BOARD_SIZE];

		for (int i = 0; i < BOARD_SIZE * 3; i++) {
			for (int j = 0; j < BOARD_SIZE * 3; j++) {
				cells[i][j] = new CellState();
				if (i == BOARD_SIZE + 2 && (j >= BOARD_SIZE + 1 || j <= BOARD_SIZE + 3)) {
					cells[i][j].isAlive = true;
				}
			}
		}

		neighbors = _neighbors;
		neighborsCalculated = new Boolean[8];
		neighborsUpdated = new Boolean[8];

		// Flush updated neighbors
		Arrays.fill(neighborsUpdated, false);
		// Flush updated neighbors
		Arrays.fill(neighborsCalculated, false);

        id = _id;

        exchangeCalculatedName = EXCHANGE_NAME_CALCULATED + id;
        exchangeUpdatedName = EXCHANGE_NAME_UPDATED + id;

		log("Initialized");
		for (int i = 0; i < 8; i++) {
			if (neighbors[i] == null) {
				System.out.print("- ");
			} else {
				System.out.print(String.valueOf(neighbors[i]) + " ");
			}

			if (i == 2 || i == 4 || i == 7) { System.out.println(); }
			if(i == 3) { System.out.print("  "); }
		}

        connect();
    }

    public void connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(exchangeCalculatedName, "fanout");
        channel.exchangeDeclare(exchangeUpdatedName, "fanout");

    }

    public void connectToNeighbors() throws IOException {
		log("Connecting to neighbors");
		int connectionCounter = 0;

        for (int i = 0; i < neighbors.length; i++) {
            if (neighbors[i] != null) {
				log("Connecting to neighbor " + String.valueOf(neighbors[i]));
				
                DeliverCallback deliverCalculatedCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    handleNeighborCalc(Integer.valueOf(message));
                };

                declareQueue(QUEUE_NAME_CALCULATED, EXCHANGE_NAME_CALCULATED, neighbors[i], deliverCalculatedCallback);

                DeliverCallback deliverUpdatedCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");

                    JSONReader reader = new JSONReader();
                    ArrayList data = (ArrayList) reader.read(message);
                    Integer index = (Integer) data.get(0);
                    ArrayList neighborState = (ArrayList) data.get(1);

                    CellState[][] cellStates = new CellState[neighborState.size()][];
                    for (int x = 0; x < neighborState.size(); x++) {
                        ArrayList row = (ArrayList) neighborState.get(x);
                        cellStates[x] = new CellState[row.size()];
                        for (int j = 0; j < row.size(); j++) {
                            HashMap cell = (HashMap) row.get(j);
                            cellStates[x][j] = new CellState((Boolean) cell.get("isAlive"));
                        }
                    }
                    handleNeighborTable(index, cellStates);
                };
                declareQueue(QUEUE_NAME_UPDATED, EXCHANGE_NAME_UPDATED, neighbors[i], deliverUpdatedCallback);

				log("Connected to " + String.valueOf(neighbors[i]));
				connectionCounter++;
            }
        }
		log("Connected to " + String.valueOf(connectionCounter) + " neigbors");
    }

	private void log(String message) {
		System.out.println("[" + String.valueOf(id) + "]: " + message);
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
        CellState[][] cellsToSend = new CellState[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                cellsToSend[i][j] = cells[i + BOARD_SIZE][j + BOARD_SIZE];
            }
        }
        Object[] data = new Object[]{id, cellsToSend};
        String message = rabbitmqJson.write(data);
        channel.basicPublish(exchangeUpdatedName, "", null, message.getBytes());
    }

	public void start() throws IOException {
		log("Starting");
		int cycleCounter = 0;


		while (cycleCounter < 10) {
			cycleCounter++;
			log("Cycle " + String.valueOf(cycleCounter));
			print();

			log("Publishing update...");

			// Notify neighbors that I am updated
			publishUpdated();

			log("Waiting for neighbors update");

			// Wait for states of neighbors to be updated
			while(!allNeighborsUpdated()) {} // TODO: get rid of busy waiting

			// Flush updated neighbors
			Arrays.fill(neighborsUpdated, false);

			log("Calculating next state");

			// Calculate next_state
			calculateAllStates();

			log("Publishing calculated...");

			// Notify neighbors that I am calculated
			publishCalculated();

			log("Waiting for neighbors calculated...");

			// Wait for all neighbors to be calculated
			while(!allNeighborsCalculated()) {} // TODO: get rid of busy waiting

			// Flush updated neighbors
			Arrays.fill(neighborsCalculated, false);

			log("Updating current state");

			updateCells();

			System.out.println();
		}
	}

	public void print() {
		// for (int i = 0; i < BOARD_SIZE; i++) {
		// 	for (int j = 0; j < BOARD_SIZE; j++) {
		// 		System.out.print(
		// 			cells[i + BOARD_SIZE][j + BOARD_SIZE].isAlive ? "X" : ".");
		// 	}
		// 	System.out.println();
		// }

		for (int j = 0; j < BOARD_SIZE * 3; j++) {
			for (int i = 0; i < BOARD_SIZE * 3; i++) {
				if  ((i < BOARD_SIZE && neighbors[Directions.L.ordinal()] == null) ||
					(i >= BOARD_SIZE * 2 && neighbors[Directions.R.ordinal()] == null) ||
					(j < BOARD_SIZE && neighbors[Directions.U.ordinal()] == null) ||
					(j >= BOARD_SIZE * 2 && neighbors[Directions.D.ordinal()] == null)) {
					
						System.out.print("__");
				} else {
					System.out.print(
					cells[i][j].isAlive ? "X " : ". ");
				}
				
			}
			System.out.println();
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
		for (int i = 0; i < 8; i++) { // TODO: do this more efficiently
			if (neighbors[i] != null && neighbors[i] == index) {
				neighborsCalculated[i] = true;
			}
		}
    }

	static public Vec2i getVectorForDirection(Directions direction) {
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
		int neighbor_dir = 0;
		for (int i = 0; i < 8; i++) { // TODO: do this more efficiently
			if (neighbors[i] != null && neighbors[i] == index) {
				neighbor_dir = i;
				break;
			}
		}
		// Coordinates of topleft corner on united board
		Vec2i delta = getVectorForDirection(Directions.values()[neighbor_dir]).add(new Vec2i(1,1)).mult(BOARD_SIZE);
		System.out.println(delta);

		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				cells[delta.x + i][delta.y + j] = neighborCells[i][j];
			}	
		}

		for (int i = 0; i < 8; i++) { // TODO: do this more efficiently
			if (neighbors[i] != null && neighbors[i] == index) {
				neighborsUpdated[i] = true;
			}
		}
    }


    private CellState calculateNextState(int x, int y) {
        int nCount = 0;

		
        for (int i = x - MARGIN_SIZE; i <= x + MARGIN_SIZE; i++) {
            for (int j = y - MARGIN_SIZE; j <= y + MARGIN_SIZE; j++) {
                if ((i != x || j != y) && getCellState(i, j).isAlive) nCount++;
            }
        }

        if (nCount == 3) return new CellState(true);
        if (nCount == 2 && getCellState(x, y).isAlive) return new CellState(true);
        return new CellState(false);
    }

    private CellState getCellState(int x, int y) {
        if (x < BOARD_SIZE && neighbors[Directions.L.ordinal()] == null)
			return new CellState();
		if (x >= BOARD_SIZE * 2 && neighbors[Directions.R.ordinal()] == null) 
			return new CellState();
		if (y < BOARD_SIZE && neighbors[Directions.U.ordinal()] == null) 
			return new CellState();
		if (y >= BOARD_SIZE * 2 && neighbors[Directions.D.ordinal()] == null) 
			return new CellState();
        return cells[x][y];
    }
}
