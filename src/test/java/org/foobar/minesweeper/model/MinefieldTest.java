/*
 * Copyright (c) 2012 Evan Flynn
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.foobar.minesweeper.model;

import com.google.common.eventbus.EventBus;
import org.foobar.minesweeper.event.SquareChangeEvent;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

/**
 * 
 * @author User
 */
public final class MinefieldTest {
  private Minefield field;
  private EventBus eventBus;

  @Before
  public void setUp() {
    eventBus = mock(EventBus.class);
    field = new Minefield(eventBus);
  }

  public void revealTwice() {
    field.reveal(0, 0);
    field.reveal(0, 0);

    verify(eventBus, times(1)).post(anyObject());
  }

  @Test
  public void testPredicates() {
    // assertTrue(field.canFlag(0, 0));
    assertTrue(field.canReveal(0, 0));
    assertFalse(field.isGameOver());
  }

  public void testFlagAfterReveal2() {
    field.reveal(0, 0);
    field.toggleFlag(0, 0);

    verify(eventBus, times(1)).post(anyObject());
    verify(eventBus, never()).post(isA(SquareChangeEvent.class));
  }

  @Test
  public void testFlag() {
    field.toggleFlag(0, 0);

    assertEquals(Square.FLAG, field.getSquareAt(0, 0));
    verify(eventBus).post(anyObject());
  }

  @Test
  public void testGameState() {
    field.reveal(0, 0);
    assertEquals(field.getGameState(), Minefield.State.PLAYING);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testGet() {
    field.getSquareAt(-1, -1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testBadReveal() {
    field.reveal(-1, -1);
  }
}
