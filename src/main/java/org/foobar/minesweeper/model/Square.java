package org.foobar.minesweeper.model;

import com.google.common.base.Objects;
import static org.foobar.minesweeper.model.SquareType.BLANK;
import static org.foobar.minesweeper.model.SquareType.FLAG;
import static org.foobar.minesweeper.model.SquareType.HITMINE;
import static org.foobar.minesweeper.model.SquareType.MINE;
import static org.foobar.minesweeper.model.SquareType.WRONGMINE;

final class Square {
  private int nearbyMines;
  private SquareType type = SquareType.BLANK;

  public Square() {}

  public Square(Square cell) {
    nearbyMines = cell.nearbyMines;
    type = cell.type;
  }

  public boolean hasNearbyMines() {
    return nearbyMines != 0;
  }

  public boolean isMine() {
    return nearbyMines == -1;
  }

  public boolean isRevealable() {
    return type == BLANK;
  }

  public void onGameLost() {
    if (type == HITMINE) {
      return;
    }

    if (isMine()) {
      type = MINE;
    } else if (type == FLAG) {
      type = WRONGMINE;
    }
  }

  public void incrementMineCount() {
    if (!isMine()) {
      nearbyMines++;
    }
  }

  public void setHitMine() {
    assert isMine();

    type = HITMINE;
  }

  public void setMine() {
    assert !isMine();

    nearbyMines = -1;
  }

  public void toggleFlag() {
    assert !type.hasMineCount();

    type = (type == FLAG) ? BLANK : FLAG;
  }

  /**
   * 
   * @return
   */
  public SquareType getType() {
    return type;
  }

  public void revealNumber() {
    assert !isMine();

    type = SquareType.of(nearbyMines);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("type", type).add("isMine", isMine())
        .add("nearbyMines", nearbyMines).toString();
  }

  public String debugNumbers() {
    return isMine() ? "*" : String.valueOf(nearbyMines);
  }
}
