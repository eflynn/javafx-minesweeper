package org.foobar.minesweeper.event;

import org.foobar.minesweeper.model.Square;

public class SquareChangeEvent {
  private final Square cell;
  private final int row;
  private final int column;

  public SquareChangeEvent(int row, int column, Square cell) {
    this.row = row;
    this.column = column;
    this.cell = cell;
  }

  public Square getCell() {
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
