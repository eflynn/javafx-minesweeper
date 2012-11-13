/*
 * Copyright 2012 Evan Flynn
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

import static com.google.common.base.Preconditions.checkElementIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.foobar.minesweeper.event.FieldHandler;
import org.foobar.minesweeper.event.HandlerRegistration;

/**
 * Provides a model for a Minesweeper game. Objects that wish to be notified with updates from this
 * class can register with the {@code EventBus}.
 *
 * @author Evan Flynn
 */
public final class Minefield {

  /**
   * The current state of the game.
   *
   * The initial state is {@code START}. It changes to {@code PLAYING} when the first square has
   * been revealed.
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

  private final int columns;
  private final int rows;
  private final int mines;
  private int unrevealed;
  private State gameState;
  private boolean gameOver;
  private final Square[][] table;
  private final List<FieldHandler> handlers = new CopyOnWriteArrayList<>();

  public Minefield() {
    this(10, 10, 10);
  }

  /**
   *
   * @param rows
   * @param columns
   * @param mines
   * @param eventBus
   */
  public Minefield(int rows, int columns, int mines) {
    this.rows = rows;
    this.columns = columns;
    this.mines = mines;

    table = new Square[rows][columns];

    initialize();
  }

  /**
   * Adds a handler for Minefield events.
   *
   * @param handler the click handler
   * @return HandlerRegistration used to remove this handler
   */
  public HandlerRegistration addFieldHandler(final FieldHandler handler) {
    handlers.add(handler);

    updateBoard();

    return new HandlerRegistration() {
      public void removeHandler() {
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
   * Gets the square at {@code row} and {@code column}
   *
   * @param row row to find {@code Square} with
   * @param column column to find {@code Square} with
   * @throws IndexOutOfBoundsException if {@code row} is negative or greater than or equal to
   *         {@code getRowCount()}
   * @throws IndexOutOfBoundsException if {@code column} is negative or greater than or equal to
   *         {@code getColumnCount()}
   */
  public Square getSquare(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(column, columns);

    return table[row][column];
  }

  /**
   * Returns true if the game was won or lost.
   *
   * @return true if the game was won or lost.
   */
  public boolean isGameOver() {
    return gameOver;
  }

  /**
   * Restarts the Minesweeper game.
   */
  public void restart() {
    initialize();
    updateBoard();
  }

  void updateSquare(Square square) {
    for(FieldHandler handler : handlers) {
      handler.updateSquare(square);
    }
  }

  void reveal(Square square) {
    if (gameState == State.START) {
      gameState = State.PLAYING;

      plantMines(square);
    }

    if (square.hit()) {
      for (Square[] columns : table) {
        for (Square i : columns)
          i.onGameLost();
      }

      gameOver = true;
      gameState = State.LOST;

      updateBoard();
    }
    else if (square.exposeNumber()) {
      unrevealed--;

//      if (checkGameWon())
//        eventBus.post(gameState);

      updateSquare(square);
    }
    else {
      visit(square);

      updateBoard();
    }
  }

  void revealNearby(Square square) {
    Square[] neighbors = findNeighbors(square);
    int nearbyFlags = 0;

    for (Square i : neighbors) {
      if (i.getType() == Squares.FLAG)
        nearbyFlags++;
    }

    if (nearbyFlags == square.getMineCount()) {
      for (Square i : neighbors) {
        if (i.getType() == Squares.BLANK)
          reveal(i);
      }
    }
  }

  private void updateBoard() {
    for (FieldHandler handler : handlers) {
      handler.updateBoard();
    }
  }

  private Square[] findNeighbors(Square square) {
    int row = square.getRow();
    int column = square.getColumn();
    Square[] neighbors = new Square[8];
    int size = 0;

    for(int r = row - 1; r <= row + 1; r++) {
      for(int c = column - 1; c <= column + 1; c++) {
        if ((r != row || c != column) && r >= 0 && c >= 0 && r < rows && c < columns)
          neighbors[size++] = table[r][c];
      }
    }

    return Arrays.copyOf(neighbors, size);
  }

  private void initialize() {
    unrevealed = (rows * columns) - mines;
    gameState = State.START;
    gameOver = false;

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++)
        table[r][c] = new Square(this, r, c);
    }
  }

  private void plantMines(Square square) {
    ArrayList<Square> list = new ArrayList<Square>(rows * columns);
    int row = square.getRow();
    int column = square.getColumn();

    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if ((r < row - 1 || r > row + 1) && (c < column - 1 || c > column + 1))
          list.add(table[r][c]);
      }
    }

    Collections.shuffle(list);

    for (Square i : list.subList(0, mines)) {
      i.plantMine();

      for (Square j : findNeighbors(i))
        j.incrementMineCount();
    }
  }

  private boolean checkGameWon() {
    boolean result = (unrevealed == 0);

    if (result) {
      gameOver = true;
      gameState = State.WON;
    }

    return result;
  }

  private void visit(Square entry) {
    entry.expose();
    unrevealed--;

    if (entry.getMineCount() == 0) {
      for (Square adj : findNeighbors(entry)) {
        if (adj.getType() != Squares.EXPOSED) {
          visit(adj);
        }
      }
    }
  }
}
