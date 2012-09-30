package org.foobar.minesweeper.event;

import org.foobar.minesweeper.model.SquareType;

public class CellChangeEvent {
    private final SquareType cell;
    private final int row;
    private final int column;
    
    public CellChangeEvent(int row, int column, SquareType cell) {
        this.row = row;
        this.column = column;
        this.cell = cell;
    }
    
    public SquareType getCell() {
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
