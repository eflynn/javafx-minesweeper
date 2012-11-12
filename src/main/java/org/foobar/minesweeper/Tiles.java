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

import org.foobar.minesweeper.model.Squares;

import javafx.scene.image.Image;

public class Tiles {
	private Tiles() { }
	
  private static final String baseDir = "resources/";
  public static final Image BLANK = loadImage("blank.png");
  public static final Image FLAG = loadImage("flag.png");
  public static final Image EXPOSED = loadImage("exposed.png");
  public static final Image NUMBERS = loadImage("numbers.png");
  public static final Image MINE = loadImage("mine.png");
  public static final Image HITMINE = loadImage("hitmine.png");
  public static final Image WRONGMINE = loadImage("wrongmine.png");

  private static Image loadImage(String path) {
    return new Image(baseDir + path);
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
}
