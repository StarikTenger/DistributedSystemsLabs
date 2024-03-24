import java.util.Optional;

public class Board {
    enum Directions {
        UL, U, UR,
        L, C, R,
        DL, D, DR
    }
    static final int BOARD_SIZE = 5;
    static final int MARGIN_SIZE = 1; // margin of processing neighbors
    private CellState[][] cells;
    private CellState[][] nextCells;

    public Board() {
        cells = new CellState[BOARD_SIZE*3][BOARD_SIZE*3];
        nextCells = new CellState[BOARD_SIZE][BOARD_SIZE];
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
