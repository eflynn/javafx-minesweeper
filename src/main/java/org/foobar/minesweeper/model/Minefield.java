package org.foobar.minesweeper.model;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import static java.util.EnumSet.of;
import java.util.List;
import java.util.Queue;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foobar.minesweeper.event.BoardChangeEvent;
import org.foobar.minesweeper.event.SquareChangeEvent;
import static org.foobar.minesweeper.model.SquareType.FLAG;

/**
 * 
 * @author User
 */
public final class Minefield {
  public enum State {
    /** The game was reset. */
    START,
    /** The game is in progress. */
    PLAYING,
    /** The game has been lost. */
    LOST,
    /** The game has been won. */
    WON;
  }

  static class Entry {    
    final int row;
    final int column;
    SquareType internal = SquareType.BLANK;
    SquareType external = SquareType.BLANK;
    int nearbyMines = -1;

    Entry(int row, int column) {
      this.row = row;
      this.column = column;
    }
  }

//  private static final EnumSet<State> playStates = of(State.START, State.PLAYING);
//  private static final Logger logger = Logger.getLogger("org.foobar.minesweeper.model.minefield");

  private final int rows;
  private final int columns;
  private final int mines;
  private final Entry[][] table;
  private final EventBus eventBus;
  private final Queue<Entry> frontier = new ArrayDeque<>(200);
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
    table = new Entry[rows][columns];

    initialize();
//    log();
  }

  public Minefield(EventBus eventBus) {
    this(eventBus, 10, 10, 10);
  }

//  public boolean canFlag(int row, int column) {
//    return !isGameOver && !getSquareAt(row, column).hasMineCount();
//  }

  /**
   * 
   * @param row
   * @param column
   * @return
   */
  public boolean canReveal(int row, int column) {
    return !isGameOver && getSquareAt(row, column) == SquareType.BLANK;
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

    return table[row][column].external;
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

    if (isGameOver || entry.external != SquareType.BLANK) {
      return;
    }

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
    
    if (isGameOver || entry.external == SquareType.NUMBER) {
      return;
    }
    
    Iterable<Entry> neighbors = findNeighbors(row, column);
    int nearbyFlags = 0;

    for (Entry i : neighbors) {
      if (i.external == SquareType.FLAG) {
        nearbyFlags++;
      }
    }

    if (nearbyFlags == countMines_(entry)) {
      for (Entry i : neighbors) {
        if (i.external == SquareType.BLANK) {
          reveal(entry);
        }
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
    
    if (entry.external == SquareType.FLAG) {
      entry.external = SquareType.BLANK;
    }
    else if (entry.external == SquareType.BLANK) {
      entry.external = SquareType.FLAG;
    }
    else {
      post = false;
    }
    
    if (post) {
      eventBus.post(new SquareChangeEvent(row, column, entry.external));
    }
  }

  private void initialize() {
    emptySquares = rows * columns;
    gameState = State.START;
    isGameOver = false;

    for(int r=0; r < rows; r++) {
      for(int c=0; c < columns; c++) {
        table[r][c] = new Entry(r, c);
      }
    }
  }

  private void reveal(Entry e) {
    if (gameState == State.START) {
      gameState = State.PLAYING;

      plantMines(e.row, e.column);
    }

    if (e.internal == SquareType.MINE) {
      e.external = SquareType.HITMINE;
      e.internal = SquareType.HITMINE;

      for(Entry[] columns : table) {
        for(Entry i : columns) {
          if (i.internal == SquareType.MINE) {
            e.external = SquareType.MINE;
          }
          else if (i.external == FLAG) {
            e.external = SquareType.WRONGMINE;
          }
        }
      }
      
      isGameOver = true;
      gameState = State.LOST;

      eventBus.post(BoardChangeEvent.INSTANCE);
      eventBus.post(gameState);
    } else {
      assert frontier.isEmpty();

      frontier.add(e);

      Entry pos;

      while ((pos = frontier.poll()) != null) {
        pos.external = SquareType.NUMBER;

        if (countMines_(pos) == 0) {
          for (Entry value : findNeighbors(pos.row, pos.column)) {
            if (value.external != SquareType.NUMBER) {
              frontier.add(value);
            }
          }
        }
      }

      eventBus.post(BoardChangeEvent.INSTANCE);
    }
  }
  
  private void addWithConstraint(Collection<Entry> collection, boolean predicate, int row, int column) {
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

  private void plantMines(int exceptRow, int exceptColumn) {
    List<Entry> list = new ArrayList<>(rows * columns);

    for(Entry[] columns : table) {
      for(Entry e : columns) {
        if (e.column != exceptRow && e.row != exceptColumn) {
          list.add(e);
        }
      }
    }
    
    Collections.shuffle(list);
//
//    for (Entry i : list) {
//      if (!i.cell.hasNearbyMines() || !i.cell.isMine()) {
//        emptySquares--;
//      }
//    }
  }

  public int countMines(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    return countMines_(table[row][column]);
  }
  
  private int countMines_(Entry entry) {
    if (entry.nearbyMines == -1) {
      int mines = 0;
      
      for(Entry e : findNeighbors(entry.row, entry.column)) {
        if (e.internal == SquareType.MINE) {
          mines++;
        }
      }

      entry.nearbyMines = mines;
    }
    
    return entry.nearbyMines;
  }

//  private void log() {
//    logger.addHandler(new ConsoleHandler());
//    logger.setLevel(Level.ALL);
//
//    StringBuilder builder = new StringBuilder();
//
//    builder.append("DEBUG\n");
//
//    for (Square[] row : table) {
//      for (Square cell : row) {
//        builder.append(cell.isMine() ? '*' : '.');
//      }
//      builder.append("\n");
//    }
//
//    logger.info(builder.toString());
//
//    builder = new StringBuilder();
//
//    for (Square[] row : table) {
//      for (Square cell : row) {
//        builder.append(cell.debugNumbers());
//        builder.append(' ');
//      }
//      builder.append("\n");
//    }
//
//    logger.info(builder.toString());
//
//    builder = new StringBuilder();
//
//    builder.append("DEBUG\n");
//
//    for (Square[] row : table) {
//      for (Square cell : row) {
//        builder.append(cell.getType().getMineCountOr(2));
//      }
//      builder.append("\n");
//    }
//    System.out.println(builder.toString());
//  }
}
