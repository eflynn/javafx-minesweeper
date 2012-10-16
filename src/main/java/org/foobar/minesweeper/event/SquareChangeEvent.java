package org.foobar.minesweeper.event;

import org.foobar.minesweeper.model.Square;

public class SquareChangeEvent {
  private final Square cell;
  private final int row;
  private final int column;

  public SquareChangeEvent(int row, int column, Square square) {
    this.row = row;
    this.column = column;
    this.cell = square;
  }

  public Square getSquare() {
    return cell;
  }

  /**
   * @return the row
   */
  public int getRow() {
    return row;
  }

  /**
   * @return the column
   */
  public int getColumn() {
    return column;
  }
}
