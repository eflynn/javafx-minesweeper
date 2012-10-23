/*
 * Copyright 2012 Evan Flynn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
