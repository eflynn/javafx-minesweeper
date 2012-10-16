package org.foobar.minesweeper.model;

import static com.google.common.base.Preconditions.checkElementIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.foobar.minesweeper.event.BoardChangeEvent;
import org.foobar.minesweeper.event.SquareChangeEvent;

import com.google.common.eventbus.EventBus;

/**
 * 
 * @author Evan Flynn
 */
public final class Minefield {
  public enum State {
    /** The game has been lost. */
    LOST,
    /** The game is in progress. */
    PLAYING,
    /** The game was reset. */
    START,
    /** The game has been won. */
    WON;
  }

  static class Entry {
    final int column;
    final int row;
    boolean mine;
    Square square = Square.BLANK;
    int nearbyMines = 0;

    Entry(int row, int column) {
      this.row = row;
      this.column = column;
    }
  }

  private final int columns;
  private int unrevealed;
  private final EventBus eventBus;
  private State gameState;
  private boolean gameOver;
  private final int mines;
  private final int rows;
  private final Entry[][] table;

  public Minefield(EventBus eventBus) {
    this(eventBus, 10, 10, 10);
  }

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
    table = new Entry[rows][columns];

    initialize();
  }

  /**
   * 
   * @param row
   * @param column
   * @return
   */
  public boolean canReveal(int row, int column) {
    return !gameOver && getSquareAt(row, column) == Square.BLANK;
  }

  public int countMines(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    return table[row][column].nearbyMines;
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
   * Gets the current game state.
   * 
   * @return the current game state.
   */
  public State getGameState() {
    return gameState;
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
   * @param row
   * @param column
   * @return
   */
  public Square getSquareAt(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    return table[row][column].square;
  }

  /**
   * 
   * 
   * @return
   */
  public boolean isGameOver() {
    return gameOver;
  }

  public void restart() {
    initialize();
    eventBus.post(BoardChangeEvent.INSTANCE);
  }

  /**
   * Reveals a cell at row and column. If the cell is a mine, the game is over. If the game is over
   * or the cell is flagged, the method returns.
   * 
   * <p>
   * Calling this method repeatedly with the same row and column will no have no effect until
   * restart() is called.
   * </p>
   * 
   * @param row the row whose cell will be revealed.
   * @param column the column whose cell will be revealed.
   * 
   * @throws IndexOutOfBoundsException if {@code row} is negative or greater than or equal to
   *         {@code getRowCount()}
   * @throws IndexOutOfBoundsException if {@code column} is negative or greater than or equal to
   *         {@code getColumnCount()}
   */
  public void reveal(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    Entry entry = table[row][column];

    if (gameOver || entry.square != Square.BLANK)
      return;

    reveal(table[row][column]);
  }

  /**
   * 
   * @param row the row whose flag will be toggled.
   * @param column the column whose flag will be toggled.
   * 
   * @throws IndexOutOfBoundsException if {@code row} is negative or greater than or equal to
   *         {@code getRowCount()}
   * @throws IndexOutOfBoundsException if {@code column} is negative or greater than or equal to
   *         {@code getColumnCount()}
   */
  public void revealNearby(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    Entry entry = table[row][column];
    
    if (gameOver || entry.square != Square.EXPOSED) {
      return;
    }
     
    Iterable<Entry> neighbors = findNeighbors(row, column);
    int nearbyFlags = 0;

    for (Entry i : neighbors) {
      if (i.square == Square.FLAG)
        nearbyFlags++;
    }
    
    if (nearbyFlags == entry.nearbyMines) {      
      for (Entry i : neighbors) {
        if (i.square == Square.BLANK)
          reveal(i);
      }      
    }
  }

  /**
   * Toggles the flag state of the cell at row and column. If the game is over or the cell cannot be
   * flagged (or unflagged), the method returns.
   * 
   * @param row the row whose flag will be toggled.
   * @param column the column whose flag will be toggled.
   * 
   * @throws IndexOutOfBoundsException if {@code row} is negative or greater than or equal to
   *         {@code getRowCount()}
   * @throws IndexOutOfBoundsException if {@code column} is negative or greater than or equal to
   *         {@code getColumnCount()}
   */
  public void toggleFlag(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    Entry entry = table[row][column];
    boolean post = true;

    if (entry.square == Square.FLAG)
      entry.square = Square.BLANK;
    else if (entry.square == Square.BLANK)
      entry.square = Square.FLAG;
    else
      post = false;

    if (post)
      eventBus.post(new SquareChangeEvent(row, column, entry.square));
  }

  private void addWithConstraint(Collection<Entry> collection, boolean predicate, int row,
      int column) {
    if (predicate) {
      collection.add(table[row][column]);
    }
  }

  private List<Entry> findNeighbors(int row, int column) {
    assert row >= 0 && row < rows : "invalid row: " + row;
    assert column >= 0 && column < columns : "invalid column: " + column;

    List<Entry> neighbors = new ArrayList<>(8);

    boolean top = row > 0;
    boolean bottom = row + 1 < rows;
    boolean left = column > 0;
    boolean right = column + 1 < columns;

    addWithConstraint(neighbors, top, row - 1, column);
    addWithConstraint(neighbors, bottom, row + 1, column);
    addWithConstraint(neighbors, left, row, column - 1);
    addWithConstraint(neighbors, right, row, column + 1);

    // diagonals
    addWithConstraint(neighbors, top && left, row - 1, column - 1);
    addWithConstraint(neighbors, top && right, row - 1, column + 1);
    addWithConstraint(neighbors, bottom && left, row + 1, column - 1);
    addWithConstraint(neighbors, bottom && right, row + 1, column + 1);

    return neighbors;
  }

  private void initialize() {
    unrevealed = (rows * columns) - mines;
    gameState = State.START;
    gameOver = false;

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++)
        table[r][c] = new Entry(r, c);
    }
  }

  private void plantMines(int row, int column) {
    List<Entry> list = new ArrayList<>(rows * columns);

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if ((r < row - 1 || r > row + 1) && (c < column - 1 || c > column + 1))
          list.add(table[r][c]);
      }
    }

    Collections.shuffle(list);
    List<Entry> mineSet = list.subList(0, mines);

    for (Entry i : mineSet) {
      i.mine = true;

      for (Entry j : findNeighbors(i.row, i.column))
        j.nearbyMines++;
    }
  }

  private void reveal(Entry entry) {
    if (gameState == State.START) {
      gameState = State.PLAYING;

      plantMines(entry.row, entry.column);
    }

    if (entry.mine) {
      entry.square = Square.HITMINE;

      for (Entry[] columns : table) {
        for (Entry i : columns) {
          if (i.mine)
            i.square = Square.MINE;
          else if (i.square == Square.FLAG)
            i.square = Square.WRONGMINE;
        }
      }

      gameOver = true;
      gameState = State.LOST;

      eventBus.post(BoardChangeEvent.INSTANCE);
      eventBus.post(gameState);
    }
    else if (entry.nearbyMines != 0) {
      setExposed(entry);

      if (checkGameWon())
        eventBus.post(gameState);

      eventBus.post(new SquareChangeEvent(entry.row, entry.column, entry.square));
    }
    else {
      visit(entry);

      eventBus.post(BoardChangeEvent.INSTANCE);

      if (checkGameWon())
        eventBus.post(gameState);
    }
  }

  private void setExposed(Entry entry) {
    unrevealed--;
    entry.square = Square.EXPOSED;
  }

  private boolean checkGameWon() {
    boolean result = (unrevealed == 0);

    if (result) {
      gameOver = true;
      gameState = State.WON;
    }

    return result;
  }

  private void visit(Entry entry) {
    setExposed(entry);

    if (entry.nearbyMines == 0) {
      for (Entry adj : findNeighbors(entry.row, entry.column)) {
        if (adj.square != Square.EXPOSED) {
          visit(adj);
        }
      }
    }
  }
}
