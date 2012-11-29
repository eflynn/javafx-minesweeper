package org.foobar.minesweeper.model;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class ModelTest {
  @SuppressWarnings("serial")
  private final Random random = new Random() {
    @Override protected int next(int bits) {
      return 1;
    }
  };

  private Minefield field;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    field = new Minefield(10, 10, 10, random);
  }

  @Test(expected=IllegalArgumentException.class)
  public void constructor() {
    field = new Minefield(-1, 0, 0);
  }

  @Test
  public void numberOfMines() {
    assertEquals(field.getMines(), 10);

    field.getSquare(0, 0).reveal();

    int mines = 0;

    for(int i=0; i<field.getRowCount(); i++) {
      for(int j=0; j<field.getColumnCount(); j++) {
        if (field.getSquare(i, j).isMine())
          mines++;
      }
    }

    assertEquals(mines, field.getMines());
  }

  @Test(expected=IndexOutOfBoundsException.class)
  public void getNegativeRow() {
    field.getSquare(-1, 0);
  }

  @Test(expected=IndexOutOfBoundsException.class)
  public void getNegativeColumn() {
    field.getSquare(0, -1);
  }
  @Test(expected=IndexOutOfBoundsException.class)
  public void getOutOfBoundsRow() {
    field.getSquare(10, 0);
  }
  @Test(expected=IndexOutOfBoundsException.class)
  public void getOutOfBoundsColumn() {
    field.getSquare(0, 10);
  }
}
