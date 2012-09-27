/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foobar.minesweeper.model;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author User
 */
public class SquareTypeTest {
    @Test
    public void testMinecount() {
        SquareType foo = SquareType.of(0);
        
        assertTrue(foo.hasMineCount());
        assertEquals(foo.getMineCount(), 0);
        assertEquals(foo.getMineCountOr(5), 0);        
    }
    
    
}
