/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foobar.minesweeper.model;

import com.google.common.eventbus.EventBus;
import org.foobar.minesweeper.event.CellChangeEvent;
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
        assertTrue(field.canFlag(0, 0));
        assertTrue(field.canReveal(0, 0));
        assertFalse(field.isGameOver());
    }
    
    @Test
    public void testFlagAfterReveal1() {
        field.reveal(0, 0);
        assertFalse(field.canFlag(0, 0));
    }
    
    public void testFlagAfterReveal2() {
        field.reveal(0, 0);
        field.toggleFlag(0, 0);
        
        verify(eventBus, times(1)).post(anyObject());
        verify(eventBus, never()).post(isA(CellChangeEvent.class));
    }
    
    @Test
    public void testFlag() {
        field.toggleFlag(0, 0);
        
        assertEquals(SquareType.FLAG, field.getSquareAt(0, 0));
        verify(eventBus).post(anyObject());
    }   
    
    @Test
    public void testGameState() {
        field.reveal(0, 0);
        assertEquals(field.getGameState(), Minefield.State.PLAYING);
    }
    
    @Test(expected=IndexOutOfBoundsException.class)
    public void testGet() {
        field.getSquareAt(-1, -1);
    }
    
    @Test(expected=IndexOutOfBoundsException.class)
    public void testBadReveal() {
        field.reveal(-1, -1);
    }
}
