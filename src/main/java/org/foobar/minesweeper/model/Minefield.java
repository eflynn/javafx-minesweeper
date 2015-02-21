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

    reset();
  }

  /**
   * Adds a handler for Minefield events.
   *
   * @param handler the field handler
   * @return {@code HandlerRegistration} used to remove this handler
   */
  public HandlerRegistration addFieldHandler(final FieldHandler handler) {
    handlers.add(handler);

    updateBoard();

    return () -> handlers.remove(handler);
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
   * Resets the Minesweeper game.
   */
  public void reset() {
    mineSet.clear();
    unrevealed = (rows * columns) - mines;

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        table[r][c] = new Square(this, r, c);
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

  void onGameLost() {
    for (Square[] columns : table) {
      for (Square square : columns) {
        square.onGameLost();
      }
    }

    updateBoard();
    setState(State.LOST);
  }

  List<Square> findNeighbors(Square square) {
    List<Square> neighbors = new ArrayList<>(8);
    int row = square.getRow();
    int column = square.getColumn();

    for (int r = row - 1; r <= row + 1; r++) {
      for (int c = column - 1; c <= column + 1; c++) {
        if ((r != row || c != column) && r >= 0 && c >= 0 && r < rows && c < columns) {
          neighbors.add(table[r][c]);
        }
      }
    }

    return neighbors;
  }

  void updateBoard() {
    handlers.forEach(FieldHandler::updateBoard);
  }

  private void cascade(Square start) {
    int exposed = start.visit();

    unrevealed -= exposed;

    if (unrevealed == 0) {
      mineSet.forEach(Square::onGameWon);

      setState(State.WON);
      updateBoard();

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

      findNeighbors(square).forEach(Square::addNearbyMine);
    }
  }

  private void setState(State state) {
    if (this.state != state) {
      this.state = state;

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
     * Called when the entire board was changed. This occurs on a reset
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
    WON
  }
}
