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

public class Square {
  private final int column;
  private final int row;
  private final Minefield minefield;
  private boolean mine;
  private Squares type = Squares.BLANK;
  private int nearbyMines;

  Square(Minefield minefield, int row, int column) {
    this.minefield = minefield;
    this.row = row;
    this.column = column;
  }

  /**
   * Gets the type of the Square.
   *
   * @return type of the Square
   */
  public Squares getType() {
    return type;
  }

  public boolean isRevealable() {
    return !minefield.isGameOver() && type == Squares.BLANK;
  }

  /**
   * Gets the row of the square.
   *
   */
  public int getRow() {
    return row;
  }

  /**
   * Gets the column of the square.
   *
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
   * Toggles the flag state of the square at row and column. If the game is over
   * or the square cannot be flagged (or unflagged), the method returns.
   *
   */
  public void toggleFlag() {
    if (minefield.isGameOver())
      return;

    if (type == Squares.FLAG)
      type = Squares.BLANK;
    else if (type == Squares.BLANK)
      type = Squares.FLAG;
    else
      return;

    minefield.updateSquare(this);
  }

  /**
   * Reveals this square. If the square is a mine, the game is over.
   * If the game is over or the square is flagged, the method returns.
   *
   * <p>
   * Calling this method repeatedly will have no effect until the game is
   * restarted.
   * </p>
   */
  public void reveal() {
    if (!minefield.isGameOver() && type == Squares.BLANK)
      minefield.reveal(this);
  }

  public void revealNearby() {
    if (!minefield.isGameOver() && type == Squares.EXPOSED)
      minefield.revealNearby(this);
  }

  void clear() {
    type = Squares.BLANK;
    mine = false;
    nearbyMines = 0;
  }

  void incrementMineCount() {
    nearbyMines++;
  }

  void plantMine() {
    mine = true;
  }

  boolean isMine() {
    return mine;
  }

  void expose() {
    type = Squares.EXPOSED;
  }

  boolean exposeNumber() {
    boolean result = nearbyMines > 0;

    if (result)
      type = Squares.EXPOSED;

    return result;
  }

  boolean hit() {
    if (mine)
      type = Squares.HITMINE;

    return mine;
  }

  void onGameLost() {
    if (mine)
      type = Squares.MINE;
    else if (type == Squares.FLAG)
      type = Squares.WRONGMINE;
  }
}
