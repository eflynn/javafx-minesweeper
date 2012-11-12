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

import static javafx.scene.input.MouseButton.MIDDLE;
import static javafx.scene.input.MouseButton.PRIMARY;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.foobar.minesweeper.event.FieldHandler;
import org.foobar.minesweeper.model.Minefield;
import org.foobar.minesweeper.model.Squares;
import org.foobar.minesweeper.model.Square;

public class MinesweeperPane extends AnchorPane {
  private static final int SQUAREW = 24;
  private static final int SQUAREH = 24;
  private final Minefield field;
  private final Canvas canvas = new Canvas(240, 240);
  private final int rows;
  private final int columns;

  private final SelectionModel select = new SelectionModel();

  public MinesweeperPane(MinesweeperPane pane) {
    this(pane.field);
  }

  public MinesweeperPane(Minefield field) {
    this.field = field;
    rows = field.getRowCount();
    columns = field.getColumnCount();

    installCanvasHandlers();

    Button newGameButton = new Button("New Game");
    Button clone = new Button("Clone");
    
    field.addFieldHandler(new FieldHandler() {
			public void updateSquare(Square square) {
				drawSquare(square);
			}

			public void updateBoard() {
				drawBoard();
			}
    });

    newGameButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        MinesweeperPane.this.field.restart();
      }
    });

    clone.setOnMouseClicked(new EventHandler<MouseEvent>() {
      public void handle(MouseEvent event) {
        Parent root = new MinesweeperPane(MinesweeperPane.this);
        
        Stage stage = new Stage();

        stage.setResizable(false);
        stage.setScene(new Scene(root, 260, 300));
        stage.setTitle("Minesweeper");
        stage.show();
      }
    });

    HBox hbox = new HBox(12);

    hbox.getChildren().addAll(newGameButton, clone);
    this.getChildren().addAll(hbox, canvas);

    AnchorPane.setTopAnchor(hbox, 12.0);
    AnchorPane.setLeftAnchor(hbox, 10.0);
    AnchorPane.setBottomAnchor(canvas, 10.0);
    AnchorPane.setLeftAnchor(canvas, 10.0);

    drawBoard();
  }
  
  private void drawSquare(Square s) {
    if (s.getType() == Squares.EXPOSED)
      drawNumber(s.getRow(), s.getColumn(), s.getMineCount());
    else {
      GraphicsContext gc = canvas.getGraphicsContext2D();
      gc.drawImage(Tiles.getImage(s.getType()), s.getColumn() * SQUAREW, s.getRow() * SQUAREH);
    }
  }
  
  private void drawBoard() {
	  for (int row = 0; row < rows; row++) {
	  	for (int column = 0; column < columns; column++)
	  		drawSquare(field.getSquare(row, column));
	  }
  }

  private void installCanvasHandlers() {

    canvas.setOnMousePressed(new MouseHandler() {
      @Override
      public void handle(MouseEvent event, int row, int column) {
      	Square square = field.getSquare(row, column);
      	
        if (event.isSecondaryButtonDown()) {
        	square.toggleFlag();
        } else if (event.isPrimaryButtonDown() && square.isRevealable()) {
          drawTile(row, column, Squares.EXPOSED);
          select.select(row, column);
        }
      }
    });

    canvas.setOnMouseClicked(new MouseHandler() {
      @Override
      public void handle(MouseEvent e, int row, int column) {
        int clicks = e.getClickCount();
        MouseButton button = e.getButton();
      	Square square = field.getSquare(row, column);

      	if (button == MIDDLE || (clicks == 2 && button == PRIMARY)) {
        	square.revealNearby();
        } else if (clicks == 1 && button == PRIMARY) {
          select.clear();
          square.reveal();
        }
      }
    });

    canvas.setOnMouseDragEntered(new MouseHandler() {
      @Override
      public void handle(MouseEvent event, int row, int column) {
        if (event.isPrimaryButtonDown()) {
          if (!select.isEmpty()) {
            drawTile(select.getSelectedRow(), select.getSelectedColumn(), Squares.BLANK);
          }

          if (field.getSquare(row, column).isRevealable()) {
            drawTile(row, column, Squares.EXPOSED);
            select.select(row, column);
          }
        }
      }
    });
  }

  private static abstract class MouseHandler implements EventHandler<MouseEvent> {
    public void handle(MouseEvent e) {
      int row = (int) e.getY() / SQUAREH;
      int col = (int) e.getX() / SQUAREW;

      handle(e, row, col);
      e.consume();
    }

    public abstract void handle(MouseEvent t, int row, int column);
  }

  private void drawTile(int row, int column, Squares square) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.drawImage(Tiles.getImage(square), column * SQUAREW, row * SQUAREH);
  }

  private void drawNumber(int row, int column, int number) {
    Rectangle2D src = new Rectangle2D(0, number * SQUAREH, SQUAREW, SQUAREH);
    Rectangle2D dest = new Rectangle2D(column * SQUAREW, row * SQUAREH, SQUAREW, SQUAREH);

    GraphicsContext gc = canvas.getGraphicsContext2D();
    drawImageSlice(gc, Tiles.NUMBERS, src, dest);
  }

  private void drawImageSlice(GraphicsContext gc, Image img, Rectangle2D srcRect, Rectangle2D destRect) {
    gc.drawImage(img, srcRect.getMinX(), srcRect.getMinY(), srcRect.getWidth(), srcRect.getHeight(),
      destRect.getMinX(), destRect.getMinY(), destRect.getWidth(), destRect.getHeight());
  }
  
}
