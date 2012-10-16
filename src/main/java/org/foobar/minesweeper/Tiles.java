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

package org.foobar.minesweeper;

import org.foobar.minesweeper.model.Square;

import javafx.scene.image.Image;

public class Tiles {
  public static final Image BLANK = new Image("blank.png");
  public static final Image FLAG = new Image("flag.png");
  public static final Image EXPOSED = new Image("exposed.png");
  public static final Image NUMBERS = new Image("numbers.png");
  public static final Image MINE = new Image("mine.png");
  public static final Image HITMINE = new Image("hitmine.png");
  public static final Image WRONGMINE = new Image("wrongmine.png");
  
  public static Image getImage(Square square) {
    switch (square) {
      case BLANK:
        return Tiles.BLANK;
      case FLAG:
        return Tiles.FLAG;
      case MINE:
        return Tiles.MINE;
      case EXPOSED:
        return Tiles.EXPOSED;
      case HITMINE:
        return Tiles.HITMINE;
      case WRONGMINE:
        return Tiles.WRONGMINE;
      default:
        throw new AssertionError("Unknown square type: " + square);
    }
  }
}
