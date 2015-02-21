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

import java.util.List;

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

  /**
   * Determines whether the square can be revealed. This is equivalent to
   * {@code !minefield.isGameOver() && getType() == Squares.BLANK}, where
   * {@code minefield} is the field this square belongs to.
   *
   * @return true if the square can be revealed.
   */
  public boolean isRevealable() {
    return !minefield.isGameOver() && type == Squares.BLANK;
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
    if (minefield.isGameOver()) {
      return;
    }

    if (type == Squares.FLAG) {
      type = Squares.BLANK;
    } else if (type == Squares.BLANK) {
      type = Squares.FLAG;
    } else {
      return;
    }

    minefield.updateSquare(this);
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
    if (type != Squares.BLANK || minefield.isGameOver()) {
      return;
    }

    if (mine) {
      type = Squares.HITMINE;
      mine = false;
      minefield.onGameLost();
    } else {
      minefield.reveal(this);
    }
  }

  /**
   * Reveals nearby squares. The square must be already exposed for this call to
   * work. Otherwise, the method returns with no change.
   */
  public void revealNearby() {
    if (minefield.isGameOver() || type != Squares.EXPOSED) {
      return;
    }

    List<Square> neighbors = minefield.findNeighbors(this);

    int nearbyFlags = neighbors.stream()
            .filter(square -> square.type == Squares.FLAG)
            .mapToInt(e -> 1)
            .sum();

    if (nearbyFlags == nearbyMines) {
      neighbors.forEach(Square::reveal);
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

  void onGameLost() {
    if (mine) {
      type = Squares.MINE;
    } else if (type == Squares.FLAG) {
      type = Squares.WRONGMINE;
    }
  }

  void onGameWon() {
    if (mine) {
      type = Squares.FLAG;
    }
  }

  int visit() {
    int exposed = 1;
    type = Squares.EXPOSED;

    if (nearbyMines == 0) {
      for (Square square : minefield.findNeighbors(this)) {
        if (square.type != Squares.EXPOSED) {
          exposed += square.visit();
        }
      }
    }

    return exposed;
  }
}
