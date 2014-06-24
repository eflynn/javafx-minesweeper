/*
 * Copyright 2012, 2013 Evan Flynn
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

import static com.google.common.base.Preconditions.checkPositionIndex;
import javafx.scene.image.Image;

import org.foobar.minesweeper.model.Squares;

public class Tiles {
  public static final Image BLANK = loadImage("blank.png");
  public static final Image FLAG = loadImage("flag.png");
  public static final Image EXPOSED = loadImage("exposed.png");
  public static final Image MINE = loadImage("mine.png");
  public static final Image HITMINE = loadImage("hitmine.png");
  public static final Image WRONGMINE = loadImage("wrongmine.png");
  private static final Image[] digits = new Image[9];

  static {
    digits[0] = EXPOSED;

    for(int i=1; i < digits.length; i++) {
      digits[i] = loadImage(String.format("number%d.png", i));
    }
  }

  private Tiles() {
  }

  public static Image getImage(Squares square) {
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

  public static Image getDigit(int index) {
    checkPositionIndex(index, 8);

    return digits[index];
  }

  private static Image loadImage(String path) {
    return new Image(Tiles.class.getResourceAsStream("/" + path));
  }
}
