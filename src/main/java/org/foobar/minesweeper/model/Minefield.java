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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.foobar.minesweeper.event.BoardChangeEvent;
import org.foobar.minesweeper.event.SquareChangeEvent;

import com.google.common.eventbus.EventBus;

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

    void onGameLost() {
      if (mine)
        square = Square.MINE;
      else if (square == Square.FLAG)
        square = Square.WRONGMINE;
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
    checkRowAndColumn(row, column);

    return !gameOver && table[row][column].square == Square.BLANK;
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
  public SquareInfo getSquareAt(int row, int column) {
    checkRowAndColumn(row, column);
    final Entry entry = table[row][column];

    return new SquareInfo() {
      public Square type() {
        return entry.square;
      }

      public int mineCount() {
        return entry.nearbyMines;
      }
    };
  }

  /**
   * Returns true if the game was won or lost.
   *
   * @return
   */
  public boolean isGameOver() {
    return gameOver;
  }

  /**
   * Restarts the Minesweeper game.
   */
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
    checkRowAndColumn(row, column);

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
    checkRowAndColumn(row, column);

    Entry entry = table[row][column];

    if (gameOver || entry.square != Square.EXPOSED) {
      return;
    }

    Entry[] neighbors = findNeighbors(row, column);
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
    checkRowAndColumn(row, column);

    Entry entry = table[row][column];

    if (entry.square == Square.FLAG)
      entry.square = Square.BLANK;
    else if (entry.square == Square.BLANK)
      entry.square = Square.FLAG;
    else
      return;

    eventBus.post(new SquareChangeEvent(row, column));
  }

  private checkRowAndColumn(int row, int column) {
    checkElementIndex(row, rows);
    checkElementIndex(colum, columns);
  }

  private Entry[] findNeighbors(int row, int column) {
    assert row >= 0 && row < rows : "invalid row: " + row;
    assert column >= 0 && column < columns : "invalid column: " + column;

    Entry[] neighbors = new Entry[8];
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
        table[r][c] = new Entry(r, c);
    }
  }

  private void plantMines(int row, int column) {
    List<Entry> list = new ArrayList<Entry>(rows * columns);

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
        for (Entry i : columns)
          i.onGameLost();
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

      eventBus.post(new SquareChangeEvent(entry.row, entry.column));
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
