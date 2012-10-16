package org.foobar.minesweeper;

public class SelectionModel {
  private int selectedRow = -1;
  private int selectedColumn = -1;
  
  public SelectionModel() {
  }
  
  public void clear() {
    selectedRow = -1;
    selectedColumn = -1;
  }
  
  public boolean isEmpty() {
    return selectedRow == -1;
  }
  
  public int getSelectedRow() {
    return selectedRow;
  }
  
  public int getSelectedColumn() {
    return selectedColumn;
  }
  
  public void select(int row, int column) {
    selectedRow = row;
    selectedColumn = column;
  }
}
