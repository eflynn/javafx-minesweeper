/*
 * Copyright 2012, 2013 Evan Flynn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.foobar.minesweeper.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides a model for a Minesweeper game. Objects that wish to be notified
 * with updates from this class can call {@code addFieldHandler}.
 *
 * This class is not thread-safe.
 *
 * @author Evan Flynn
 */
public final class Minefield {
  private final int columns;
  private final int rows;
  private final int mines;
  private int unrevealed;
  private State state;
  private final Square[][] table;
  private final List<FieldHandler> handlers = new CopyOnWriteArrayList<>();
  private final List<Square> mineSet = new ArrayList<>();
  private final Random random;

  /**
   * Creates a {@code Minefield}.
   *
   * @param rows the number of rows in the {@code Minefield}
   * @param columns the number of columns in the {@code Minefield}
   * @param mines the number of mines in the {@code Minefield}
   * @throws IllegalArgumentException if {@code rows}, {@code columns}, or
   *           {@code mines} is negative.
   */
  public Minefield(int rows, int columns, int mines) {
    this(rows, columns, mines, new Random());
  }

  Minefield(int rows, int columns, int mines, Random random) {
    // FIXME: only does basic checks for sanity.

    checkArgument(rows > 0, "rows must be positive: %s", rows);
    checkArgument(columns > 0, "columns must be positive: %s", columns);
    checkArgument(mines > 0, "mines must be positive: %s", mines);

    this.rows = rows;
    this.columns = columns;
    this.mines = mines;
    this.random = random;

    table = new Square[rows][columns];

    restart();
  }

  /**
   * Adds a handler for Minefield events.
   *
   * @param handler the click handler
   * @return {@code HandlerRegistration} used to remove this handler
   */
  public HandlerRegistration addFieldHandler(final FieldHandler handler) {
    handlers.add(handler);

    updateBoard();

    return new HandlerRegistration() {
      @Override public void removeHandler() {
        handlers.remove(handler);
      }
    };
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
  public State getState() {
    return state;
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
   * Gets the square at {@code row} and {@code column}.
   *
   * @param row row to find {@code Square} with
   * @param column column to find {@code Square} with
   * @throws IndexOutOfBoundsException if {@code row} is negative or greater
   *           than or equal to {@code getRowCount()}
   * @throws IndexOutOfBoundsException if {@code column} is negative or greater
   *           than or equal to {@code getColumnCount()}
   * @return square at {@code row} and {@code column}
   */
  public Square getSquare(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    return table[row][column];
  }

  /**
   * Is the game over?
   *
   * @return whether the game is over
   */
  public boolean isGameOver() {
    return (state == State.LOST || state == State.WON);
  }

  /**
   * Restarts the Minesweeper game.
   */
  public void restart() {
    mineSet.clear();
    unrevealed = (rows * columns) - mines;

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        table[r][c] = new Square(r, c);
      }
    }

    updateBoard();
    setState(State.START);
  }

  void updateSquare(Square square) {
    for (FieldHandler handler : handlers) {
      handler.updateSquare(square);
    }
  }

  void reveal(Square square) {
    assert !isGameOver() && square.getType() == Squares.BLANK;

    if (state == State.START) {
      firstClick(square);
    }

    cascade(square);
  }

  List<Square> findNeighbors(Square square) {
    List<Square> neighbors = new ArrayList<>(8);
    int row = square.getRow();
    int column = square.getColumn();

    for (int r = row - 1; r <= row + 1; r++) {
      for (int c = column - 1; c <= column + 1; c++) {
        if ((r != row || c != column) && r >= 0 && c >= 0 && r < rows
            && c < columns)
          neighbors.add(table[r][c]);
      }
    }

    return neighbors;
  }

  void updateBoard() {
    for (FieldHandler handler : handlers) {
      handler.updateBoard();
    }
  }

  private void cascade(Square start) {
    int exposed = start.visit();

    unrevealed -= exposed;

    if (unrevealed == 0) {
      setState(State.WON);
    } else if (exposed == 1) {
      updateSquare(start);
    } else {
      updateBoard();
    }
  }

  private void firstClick(Square first) {
    setState(State.PLAYING);

    List<Square> flat = new ArrayList<>(rows * columns);

    for(int i = 0; i < rows; i++) {
      for(int j = 0; j < columns; j++) {
        if (table[i][j] != first) {
          flat.add(table[i][j]);
        }
      }
    }

    Collections.shuffle(flat, random);

    mineSet.addAll(flat.subList(0, mines));

    for(Square square : mineSet) {
      square.setMine(true);

      for (Square neighbor : findNeighbors(square)) {
        neighbor.addNearbyMine();
      }
    }
  }

  private void setState(State state) {
    if (this.state != state) {
      // TODO: check if board update always applies.
      this.state = state;
      updateBoard();

      for (FieldHandler handler : handlers) {
        handler.changeState(state);
      }
    }
  }

  /**
   * Handler for {@code Minefield} events.
   */
  public interface FieldHandler {
    /**
     * Called when a {@code Square} was changed.
     *
     * @param square  square to update.
     */
    void updateSquare(Square square);

    /**
     * Called when the entire board was changed. This occurs on a restart
     * or a cascade.
     */
    void updateBoard();

    /**
     * Called when the game state was updated.
     */
    void changeState(State state);
  }

  /**
   * The current state of the game.
   *
   * The initial state is {@code START}. It changes to {@code PLAYING} when the
   * first square has been revealed.
   */
  public enum State {
    /** Indicates that a mine was tripped and the game is over. */
    LOST,
    /** The game is in progress. */
    PLAYING,
    /** The game was reset. */
    START,
    /** The game has been won. */
    WON;
  }

  public class Square {
    private final int column;
    private final int row;
    private boolean mine;
    private Squares type = Squares.BLANK;
    private int nearbyMines;
    private boolean cached;

    Square(int row, int column) {
      this.row = row;
      this.column = column;
    }

    /**
     * Gets the type of the Square.
     *
     * @return type of the Square
     */
    public Squares getType() {
      if (!cached && isGameOver()) {
        if (mine && getState() == State.LOST) {
          type = Squares.MINE;
        } else if (type == Squares.FLAG && getState() == State.LOST) {
          type = Squares.WRONGMINE;
        } else if (getState() == State.WON && mine) {
          type = Squares.FLAG;
        }

        cached = true;
      }

      return type;
    }

    /**
     * Determines whether the square can be revealed. This is equivalent to
     * {@code !minefield.isGameOver() && getType() == Squares.BLANK}, where
     * {@code minefield} is the field this square belongs to.
     *
     * @return true if the square can be revealed.
     */
    public boolean isRevealable() {
      return !isGameOver() && type == Squares.BLANK;
    }

    /**
     * Gets the row of the square.
     *
     * @return the row of the square
     */
    public int getRow() {
      return row;
    }

    /**
     * Gets the column of the square.
     *
     * @return the column of the square.
     */
    public int getColumn() {
      return column;
    }

    /**
     * Gets the number of nearby mines.
     *
     */
    public int getMineCount() {
      return nearbyMines;
    }

    /**
     * Toggles the flag state of the square. If the game is over or the square
     * cannot be flagged, the method returns.
     *
     */
    public void toggleFlag() {
      if (isGameOver()) {
        return;
      }

      if (type == Squares.FLAG) {
        type = Squares.BLANK;
      } else if (type == Squares.BLANK) {
        type = Squares.FLAG;
      } else {
        return;
      }

      updateSquare(this);
    }

    /**
     * Reveals this square. If the square is a mine, the game is over. If the game
     * is over or the square is flagged, the method returns.
     *
     * <p>
     * Calling this method repeatedly will have no effect until the game is
     * restarted.
     */
    public void reveal() {
      if (type != Squares.BLANK || isGameOver()) {
        return;
      }

      if (mine) {
        mine = false;
        type = Squares.HITMINE;
        setState(State.LOST);
      } else {
        Minefield.this.reveal(this);
      }
    }

    /**
     * Reveals nearby squares. The square must be already exposed for this call to
     * work. Otherwise, the method returns with no change.
     */
    public void revealNearby() {
      if (isGameOver() || type != Squares.EXPOSED) {
        return;
      }

      List<Square> neighbors = findNeighbors(this);
      int nearbyFlags = 0;

      for (Square square : neighbors) {
        if (square.type == Squares.FLAG) {
          nearbyFlags++;
        }
      }

      if (nearbyFlags == nearbyMines) {
        for (Square square : neighbors) {
          square.reveal();
        }
      }
    }

    void addNearbyMine() {
      nearbyMines++;
    }

    boolean isMine() {
      return mine;
    }

    void setMine(boolean isMine) {
      mine = isMine;
    }

    int visit() {
      int exposed = 1;
      type = Squares.EXPOSED;

      if (nearbyMines == 0) {
        for (Square square : findNeighbors(this)) {
          if (square.type != Squares.EXPOSED) {
            exposed += square.visit();
          }
        }
      }

      return exposed;
    }
  }

}
