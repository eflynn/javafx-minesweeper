package org.foobar.minesweeper.model;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.eventbus.EventBus;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import static java.util.EnumSet.of;
import java.util.List;
import java.util.Queue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foobar.minesweeper.event.BoardChangeEvent;
import org.foobar.minesweeper.event.CellChangeEvent;
import static org.foobar.minesweeper.model.SquareType.FLAG;

/**
 *
 * @author User
 */
public final class Minefield {
    public enum State {
        /**
         * The game was reset.
         */
        START,
        /**
         * The game is in progress.
         */
        PLAYING,
        /**
         * The game has been lost.
         */
        LOST,
        /**
         * The game has been won.
         */
        WON;
    }

    static class Entry {
        final int row;
        final int column;
        final Square cell;

        Entry(int row, int column, Square cell) {
            this.row = row;
            this.column = column;
            this.cell = cell;
        }
    }
    private static final EnumSet<State> playStates =
            of(State.START, State.PLAYING);
    private static final Logger logger =
            Logger.getLogger("org.foobar.minesweeper.model.minefield");

    private final int rows;
    private final int columns;
    private final int mines;
    private final Square[][] table;
    private final EventBus eventBus;
    private final Queue<Entry> queue = new ArrayDeque<>(200);
    private State gameState;
    private boolean isGameOver;
    private int emptySquares;

    /**
     *
     * @param rows
     * @param columns
     * @param mines
     * @param eventBus
     */
    public Minefield(EventBus eventBus, int rows, int columns, int mines) {
        this.eventBus = eventBus;
        this.rows = rows;
        this.columns = columns;
        this.mines = mines;
        table = new Square[rows + 2][columns + 2];

        initialize();
        log();
    }

    public Minefield(EventBus eventBus) {
        this(eventBus, 10, 10, 10);
    }

    public boolean canFlag(int row, int column) {
        return !isGameOver && !getSquareAt(row, column).hasMineCount();
    }

    /**
     *
     * @param row
     * @param column
     * @return
     */
    public boolean canReveal(int row, int column) {
        return !isGameOver && getSquareAt(row, column).isRevealable();
    }

    /**
     *
     *
     * @return
     */
    public boolean isGameOver() {
        return isGameOver;
    }

    /**
     * Gets the current game state.
     *
     * @return the current game state.
     */
    public State getGameState() {
        return gameState;
    }

    /**
     *
     * @param row
     * @param column
     * @return
     */
    public SquareType getSquareAt(int row, int column) {
        checkElementIndex(row, rows);
        checkElementIndex(column, columns);

        return table[row + 1][column + 1].getType();
    }

    /**
     * Gets the number of columns in the minefield.
     *
     * @return the number of columns in the minefield.
     */
    public int getColumnCount() {
        return columns;
    }

    /**
     * Gets number of mines
     *
     * @return the mines
     */
    public int getMines() {
        return mines;
    }

    /**
     * Gets number of rows
     *
     * @return number of rows
     */
    public int getRowCount() {
        return rows;
    }

    /**
     *
     */
    public void restart() {
        initialize();
        eventBus.post(BoardChangeEvent.INSTANCE);
    }

    /**
     * Reveals a cell at row and column. If the cell is a mine, the game is
     * over. If the game is over or the cell is flagged, the method returns.
     *
     * <p>Calling this method repeatedly with the same row and column will no
     * have no effect until restart() is called.</p>
     *
     * @param row the row whose cell will be revealed.
     * @param column the column whose cell will be revealed.
     *
     * @throws IndexOutOfBoundsException if {@code row} is negative or greater
     * than or equal to {@code getRowCount()}
     * @throws IndexOutOfBoundsException if {@code column} is negative or greater
     * than or equal to {@code getColumnCount()}
     */
    public void reveal(int row, int column) {
        checkElementIndex(row, rows);
        checkElementIndex(column, columns);

        row++;
        column++;

        Square cell = table[row][column];

        if (!playStates.contains(gameState) || !cell.isRevealable()) {
            return;
        }

        reveal(new Entry(row, column, cell));
    }

    /**
     *
     * @param row the row whose flag will be toggled.
     * @param column the column whose flag will be toggled.
     *
     * @throws IndexOutOfBoundsException if {@code row} is negative or greater
     * than or equal to {@code getRowCount()}
     * @throws IndexOutOfBoundsException if {@code column} is negative or greater
     * than or equal to {@code getColumnCount()}
     */
    public void revealNearby(int row, int column) {
        Entry entry = getEntry(row, column);
        checkState(playStates.contains(gameState) && entry.cell.getType().hasMineCount());

        int nearbyFlags = 0;

        Iterable<Entry> neighbors = findNeighbors(entry.row, entry.column);

        for (Entry i : neighbors) {
            if (i.cell.getType() == FLAG) {
                nearbyFlags++;
            }
        }

        if (nearbyFlags == entry.cell.getType().getMineCount()) {
            for (Entry i : neighbors) {
                if (i.cell.isRevealable()) {
                    reveal(entry);
                }
            }
        }
    }

    /**
     * Toggles the flag state of the cell at row and column. If the game is over
     * or the cell cannot be flagged (or unflagged), the method returns.
     *
     * @param row the row whose flag will be toggled.
     * @param column the column whose flag will be toggled.
     *
     * @throws IndexOutOfBoundsException if {@code row} is negative or greater
     * than or equal to {@code getRowCount()}
     * @throws IndexOutOfBoundsException if {@code column} is negative or greater
     * than or equal to {@code getColumnCount()}
     */
    public void toggleFlag(int row, int column) {
        checkElementIndex(row, rows);
        checkElementIndex(column, columns);

        Square cell = table[row + 1][column + 1];
        cell.toggleFlag();

        eventBus.post(new CellChangeEvent(row, column, cell.getType()));
    }

    private Entry getEntry(int row, int column) {
        checkElementIndex(row, rows);
        checkElementIndex(column, columns);

        row++;
        column++;

        return new Entry(row, column, table[row][column]);
    }

    private void initialize() {
        emptySquares = rows * columns;
        gameState = State.START;
        isGameOver = false;

        Square edge = new Square();
        edge.revealNumber();

        for (int i = 1; i < rows + 1; i++) {
            table[i][0] = new Square(edge);
            table[i][columns + 1] = new Square(edge);

            for (int j = 1; j < columns + 1; j++) {
                table[i][j] = new Square();
            }
        }

        for (int i = 0; i < columns + 2; i++) {
            table[0][i] = new Square(edge);
        }

        for (int i = 0; i < columns + 2; i++) {
            table[rows + 1][i] = new Square(edge);
        }
    }

    private void reveal(Entry e) {
        if (gameState == State.START) {
            gameState = State.PLAYING;

            plantMines(e.row, e.column);
        }

        if (e.cell.isMine()) {
            e.cell.setHitMine();

            for (int r = 1; r < rows + 1; r++) {
                for (int c = 1; c < columns + 1; c++) {
                    table[r][c].onGameLost();
                }
            }

            isGameOver = true;
            gameState = State.LOST;

            eventBus.post(BoardChangeEvent.INSTANCE);
            eventBus.post(gameState);
        }
        else {
            assert queue.isEmpty();

            queue.add(e);

            Entry pos;

            while ((pos = queue.poll()) != null) {
                pos.cell.revealNumber();

                enqueueNearby(pos);
            }

            eventBus.post(BoardChangeEvent.INSTANCE);
        }
    }

    private void enqueueNearby(Entry pos) {
        if (pos.cell.getType().getMineCount() == 0) {
            for (Entry value : findNeighbors(pos.row, pos.column)) {
                if (!value.cell.getType().hasMineCount()) {
                    queue.add(value);
                }
            }
        }
    }

    private Iterable<Entry> findNeighbors(int row, int column) {
        assert row > 0 && row <= rows : "invalid row: " + row;
        assert column > 0 && column <= columns : "invalid column: " + column;

        List<Entry> neighbors = new ArrayList<>(8);
        
        for(int c = column - 1; c <= column + 1; c++) {
            neighbors.add(new Entry(row + 1, c, table[row + 1][c]));
        }
        
        for(int c = column - 1; c <= column + 1; c++) {
            neighbors.add(new Entry(row - 1, c, table[row - 1][c]));
        }
        
        neighbors.add(new Entry(row, column - 1, table[row][column - 1]));
        neighbors.add(new Entry(row, column + 1, table[row][column + 1]));

        return neighbors;
    }

    private void plantMines(int exceptRow, int exceptColumn) {
        List<Entry> list = new ArrayList<>(rows * columns);

        for (int r = 1; r < rows + 1; r++) {
            for (int c = 1; c < columns + 1; c++) {
                if (exceptRow != r || exceptColumn != c) {
                    list.add(new Entry(r, c, table[r][c]));
                }
            }
        }

        Collections.shuffle(list);

        for (Entry origin : list.subList(0, mines)) {
            origin.cell.setMine();

            for (Entry i : findNeighbors(origin.row, origin.column)) {
                i.cell.incrementMineCount();
            }
        }
        
        for (Entry i : list) {
            if (!i.cell.hasNearbyMines() || !i.cell.isMine()) {
                emptySquares--;
            }
        }
    }

    private void log() {
        logger.addHandler(new ConsoleHandler());
        logger.setLevel(Level.ALL);

        StringBuilder builder = new StringBuilder();

        builder.append("DEBUG\n");

        for (Square[] row : table) {
            for (Square cell : row) {
                builder.append(cell.isMine() ? '*' : '.');
            }
            builder.append("\n");
        }

        logger.info(builder.toString());

        builder = new StringBuilder();

        for (Square[] row : table) {
            for (Square cell : row) {
                builder.append(cell.debugNumbers());
                builder.append(' ');
            }
            builder.append("\n");
        }

        logger.info(builder.toString());

        builder = new StringBuilder();

        builder.append("DEBUG\n");

        for (Square[] row : table) {
            for (Square cell : row) {
                builder.append(cell.getType().getMineCountOr(2));
            }
            builder.append("\n");
        }
        System.out.println(builder.toString());
    }

    private Square lookupCell(int row, int column) {
        return table[row + 1][column + 1];
    }
}
