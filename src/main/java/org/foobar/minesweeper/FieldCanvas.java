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

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class FieldCanvas extends Canvas {
  public static final double SQUAREW = 24.0;
  public static final double SQUAREH = 24.0;

  private final SelectionModel select = new SelectionModel();
  private double translateX = 0.0;
  private double translateY = 0.0;

  public FieldCanvas() {
  }

  public void setSelection(int row, int column) {
    setLocation(row, column);
    drawImage(Tiles.EXPOSED);
    select.select(row, column);
  }

  public void clearSelection() {
    setLocation(select.getRow(), select.getColumn());
    drawImage(Tiles.BLANK);
    select.clear();
  }

  public int scaleRow(double y) {
    return (int) (y / SQUAREH);
  }

  public int scaleColumn(double x) {
    return (int) (x / SQUAREW);
  }

  public void setLocation(int row, int column) {
    translateX = column * SQUAREW;
    translateY = row * SQUAREH;
  }

  public void drawImage(Image image) {
    getGraphicsContext2D().drawImage(image, translateX, translateY);
  }
}
