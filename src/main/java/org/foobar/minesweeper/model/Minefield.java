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
import java.util.concurrent.CopyOnWriteArrayList;

import org.foobar.minesweeper.events.HandlerRegistration;
import org.foobar.minesweeper.events.ChangeHandler;

/**
 * Provides a model for a Minesweeper game. Objects that wish to be notified
 * with updates from this class can call {@code addChangeHandler}.
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
  private final List<ChangeHandler> handlers = new CopyOnWriteArrayList<>();
  private final List<Square> mineSet = new ArrayList<>();

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
    // FIXME: only does basic checks for sanity.

    checkArgument(rows > 0, "rows must be positive: %s", rows);
    checkArgument(columns > 0, "columns must be positive: %s", columns);
    checkArgument(mines > 0, "mines must be positive: %s", mines);

    this.rows = rows;
    this.columns = columns;
    this.mines = mines;

    table = new Square[rows][columns];

    reset();
  }

  /**
   * Adds a handler for Minefield events.
   *
   * @param handler the change handler
   * @return {@code HandlerRegistration} used to remove this handler
   */
  public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
    handlers.add(handler);

    update();

    return new HandlerRegistration() {
      @Override public void removeHandler() {
        handlers.remove(handler);
      }
    };
  }

  public Cursor cursor() {
    return new Cursor(0, 0);
  }

  public Cursor cursor(int row, int column) {
    return new Cursor(row, column);
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
   * Is the game over?
   *
   * @return whether the game is over
   */
  public boolean isGameOver() {
    return state.isGameOver();
  }

  /**
   * Resets the Minesweeper game.
   */
  public void reset() {
    mineSet.clear();
    unrevealed = (rows * columns) - mines;

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        table[r][c] = new Square(r, c);
      }
    }

    update();
    setState(State.START);
  }

  private void reveal(Square square) {
    if (isGameOver() || !square.type.isBlank()) {
      return;
    }

    if (square.tripMine()) {
      onGameLost();
    } else {
      if (state == State.START) {
        firstClick(square);
      }

      cascade(square);
    }
  }

  private void revealNearby(Square source) {
    if (isGameOver() || source.type.isExposed()) {
      return;
    }

    List<Square> neighbors = findNeighbors(source);
    int nearbyFlags = 0;

    for (Square square : neighbors) {
      if (square.type.isFlagged()) {
        nearbyFlags++;
      }
    }

    if (nearbyFlags == source.nearbyMines) {
      for (Square square : neighbors) {
        reveal(square);
      }
    }
  }

  private void onGameLost() {
    for (Square[] columns : table) {
      for (Square square : columns) {
        square.revealOnLose();
      }
    }

    update();
    setState(State.LOST);
  }

  private boolean withinBounds(int row, int column) {
    return (row >= 0 && column >= 0) && (row < rows && column < columns);
  }

  private List<Square> findNeighbors(Square square) {
    List<Square> neighbors = new ArrayList<>(8);

    for (int r = square.row - 1; r <= square.row + 1; r++) {
      for (int c = square.column - 1; c <= square.column + 1; c++) {
        if ((r != square.row || c != square.column) && withinBounds(r, c)) {
          neighbors.add(table[r][c]);
        }
      }
    }

    return neighbors;
  }

  private void update() {
    for (ChangeHandler handler : handlers) {
      handler.onUpdate();
    }
  }

  private void cascade(Square start) {
    int exposed = visit(start);

    unrevealed -= exposed;

    if (unrevealed == 0) {
      // Game is won
      for(Square square : mineSet) {
        square.type = Squares.FLAG;
      }

      setState(State.WON);
      update();

    } else if (exposed == 1) {
      update();
    } else {
      update();
    }
  }

  private int visit(Square source) {
    int exposed = 1;
    source.expose();

    if (source.nearbyMines == 0) {
      for (Square square : findNeighbors(source)) {
        if (!square.type.isExposed()) {
          exposed += visit(square);
        }
      }
    }

    return exposed;
  }


  private void firstClick(Square first) {
    setState(State.PLAYING);

    List<Square> flat = new ArrayList<>(rows * columns);

    for(int i = 0; i < rows; i++) {
      for(int j = 0; j < columns; j++) {
        if (i != first.row || j != first.column) {
          flat.add(table[i][j]);
        }
      }
    }

    Collections.shuffle(flat);

    mineSet.addAll(flat.subList(0, mines));

    for(Square square : mineSet) {
      square.mine = true;

      for (Square neighbor : findNeighbors(square)) {
        neighbor.nearbyMines++;
      }
    }
  }

  private void setState(State state) {
    if (this.state != state) {
      this.state = state;

      update();
    }
  }

  static final class Square {
    private final int row;
    private final int column;
    private Squares type = Squares.BLANK;
    private int nearbyMines;
    private boolean mine;

    Square(int row, int column) {
      this.row = row;
      this.column = column;
    }

    void expose() {
      type = Squares.EXPOSED;
    }

    boolean tripMine() {
      if (mine) {
        type = Squares.HITMINE;
      }

      return mine;
    }

    boolean toggleFlag() {
      boolean changed = true;

      if (type == Squares.FLAG) {
        type = Squares.BLANK;
      } else if (type == Squares.BLANK) {
        type = Squares.FLAG;
      } else {
        changed = false;
      }

      return changed;
    }

    void revealOnLose() {
      if (mine && type != Squares.HITMINE) {
        type = Squares.MINE;
      } else if (type == Squares.FLAG) {
        type = Squares.WRONGMINE;
      }
    }
  }

  public class Cursor {
    private int row;
    private int column;

    Cursor(int row, int column) {
      this.row = row;
      this.column = column;
    }

    public void moveTo(int row, int column) {
      this.row = row;
      this.column = column;
    }

    public int getRow() {
      return row;
    }

    public boolean isRevealable() {
      return !isGameOver() && table[row][column].type.isBlank();
    }

    public int getColumn() {
      return column;
    }

    public Squares getType() {
      return table[row][column].type;
    }

    public int getNearbyMineCount() {
      return table[row][column].nearbyMines;
    }

    public void toggleFlag() {
      if (isGameOver()) {
        return;
      }

      if (!isGameOver() && table[row][column].toggleFlag()) {
        update();
      }
    }

    public void revealNearby() {
      Minefield.this.revealNearby(table[row][column]);
    }

    public void reveal() {
      Minefield.this.reveal(table[row][column]);
    }
  }

  /**
   * The current state of the game.
   *
   * The initial state is {@code START}. It changes to {@code PLAYING} when the
   * first square has been revealed.
   */
  public enum State {
    /** The game is in progress. */
    PLAYING(false),
    /** The game was reset. */
    START(false),
    /** The game has been won. */
    WON(true),
    /** Indicates that a mine was tripped and the game is over. */
    LOST(true);

    private final boolean gameOver;

    State(boolean gameOver) {
      this.gameOver = gameOver;
    }

    public boolean isGameOver() {
      return gameOver;
    }
  }
}
