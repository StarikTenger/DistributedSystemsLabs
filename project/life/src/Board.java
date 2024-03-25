import java.util.Optional;

public class Board {
    enum Directions {
        UL, U, UR,
        L,     R,
        DL, D, DR
    }
    static final int BOARD_SIZE = 5;
    static final int MARGIN_SIZE = 1; // margin of processing neighbors
    private CellState[][] cells;
    private CellState[][] nextCells;
	private running = true;
	private Boolean[] neighborsCalculated;
	private Boolean[] neighborsUpdated;

    public Board() {
        cells = new CellState[BOARD_SIZE * 3][BOARD_SIZE * 3];
        nextCells = new CellState[BOARD_SIZE][BOARD_SIZE];
		neighborsCalculated = new Boolean[8];
		neighborsUpdated = new Boolean[8];
    }

	public void start() {
		while (running) {
			// Flush updated neighbors
			Arrays.fill(neighborsUpdated, false);

			// TODO: Notify neighbors that I am updated

			// Wait for states of neighbors to be updated
			while(!allNeighborsUpdated) {} // TODO: get rid of busy waiting

			calculateAllStates();

			// TODO: Notify neighbors that I am calculated

			// Wait for all neighbors to be calculated
			while(!allNeighborsCalculated()) {} // TODO: get rid of busy waiting

			updateCells();
		}
	}

	private Boolean allNeighborsCalculated() {
		for (int i = 0; i < 8; i++) {
			if (!neighborsCalculated[i]) return false;
		}
		return true;
	}

	private Boolean allNeighborsUpdated() {
		for (int i = 0; i < 8; i++) {
			if (!neighborsUpdated[i]) return false;
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

	private Vec2i getVectorForDirection(Direction direction) {
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
		Vec2i delta = getVectorForDirection(index).add({1,1}).mult(BOARD_SIZE);

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
