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

import org.foobar.minesweeper.event.FieldHandler;
import org.foobar.minesweeper.model.Minefield;
import org.foobar.minesweeper.model.Square;
import org.foobar.minesweeper.model.Squares;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public final class MinesweeperController {
  private static final int SQUAREW = 24;
  private static final int SQUAREH = 24;
  private final int rows;
  private final int columns;
  
  @FXML
  private Button btnClone;

  @FXML
  private Button btnNewGame;
  
  @FXML
  private Canvas canvas;
  
  private Minefield field;
  
  private final SelectionModel select = new SelectionModel();
  
  public MinesweeperController() {
    rows = 10;
    columns = 10;
  }

  @FXML
  public void initialize() {
    field = new Minefield(rows, columns, 10);

    field.addFieldHandler(new FieldHandler() {
      public void updateSquare(Square square) {
        drawSquare(square);
      }

      public void updateBoard() {
        drawBoard();
      }
    });
  }
  
  @FXML
  public void newGame(ActionEvent event) {
    field.restart();
  }
  
  private void drawSquare(Square square) {
    GraphicsContext context = canvas.getGraphicsContext2D();

    if (square.getType() == Squares.EXPOSED) {
      context.save();
      context.translate(square.getColumn() * SQUAREW, square.getRow() * SQUAREH);
      drawImageSlice(context, Tiles.NUMBERS, square.getMineCount() * SQUAREH);
      context.restore();
    }
    else {
      context.drawImage(Tiles.getImage(square.getType()), square.getColumn() * SQUAREW, square.getRow() * SQUAREH);
    }
  }
  
  private void drawBoard() {
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++)
        drawSquare(field.getSquare(row, column));
    }
  }

  
  private Square findSquare(MouseEvent event) {
    int row = (int) event.getY() / SQUAREH;
    int column = (int) event.getX() / SQUAREW;
    
    return field.getSquare(row, column);
  }
  
  @FXML
  private void onCanvasClick(MouseEvent event) {
    Square square = findSquare(event);
    int clicks = event.getClickCount();
    MouseButton button = event.getButton();

    if (button == MouseButton.MIDDLE || (clicks == 2 && button == MouseButton.PRIMARY)) {
      square.revealNearby();
    } else if (clicks == 1 && button == MouseButton.PRIMARY) {
      select.clear();
      square.reveal();
    }
  }
  
  @FXML
  private void onCanvasPressed(MouseEvent event) {
    Square square = findSquare(event);
    int row = square.getRow();
    int column = square.getColumn();
    
    if (event.isSecondaryButtonDown()) {
      square.toggleFlag();
    } else if (event.isPrimaryButtonDown() && square.isRevealable()) {
      drawTile(row, column, Squares.EXPOSED);
      select.select(row, column);
    }
  }
  
  private void drawTile(int row, int column, Squares square) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.drawImage(Tiles.getImage(square), column * SQUAREW, row * SQUAREH);
  }

  private void drawImageSlice(GraphicsContext gc, Image img, double ySrc) {
    gc.drawImage(img, 0, ySrc, SQUAREW, SQUAREH, 0, 0, SQUAREW, SQUAREH);
  }
}
