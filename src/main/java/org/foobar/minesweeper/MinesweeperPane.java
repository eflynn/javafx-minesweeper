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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.foobar.minesweeper.event.BoardChangeEvent;
import org.foobar.minesweeper.event.SquareChangeEvent;
import org.foobar.minesweeper.model.Minefield;
import org.foobar.minesweeper.model.Square;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class MinesweeperPane extends AnchorPane {
  public static final int SQUAREW = 24;
  public static final int SQUAREH = 24;
  private final Minefield field;
  private final Canvas canvas = new Canvas(240, 240);
  private final EventBus eventBus;

  private final SelectionModel select = new SelectionModel();

  public MinesweeperPane(MinesweeperPane pane) {
    this(pane.field, pane.eventBus);
  }

  public MinesweeperPane(Minefield field, final EventBus eventBus) {
    this.field = field;
    this.eventBus = eventBus;

    installCanvasHandlers();

    Button newGameButton = new Button("New Game");
    Button clone = new Button("Clone");

    newGameButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        MinesweeperPane.this.field.restart();
      }
    });

    clone.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        Parent root = new MinesweeperPane(MinesweeperPane.this);
        eventBus.register(root);

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

    updateBoard(BoardChangeEvent.INSTANCE);
  }

  private void installCanvasHandlers() {

    canvas.setOnMousePressed(new MouseHandler() {
      @Override
      public void handle(MouseEvent event, int row, int column) {
        if (event.isSecondaryButtonDown()) {
          field.toggleFlag(row, column);
        } else if (event.isPrimaryButtonDown() && field.canReveal(row, column)) {
          drawTile(row, column, Square.EXPOSED);
          select.select(row, column);
        }
      }
    });

    canvas.setOnMouseClicked(new MouseHandler() {
      @Override
      public void handle(MouseEvent e, int row, int column) {
        int clicks = e.getClickCount();
        MouseButton button = e.getButton();

        if ((clicks == 2 && button == PRIMARY) || button == MIDDLE) {
          field.revealNearby(row, column);
        } else if (clicks == 1 && button == PRIMARY) {
          select.clear();
          field.reveal(row, column);
        }
      }
    });

    canvas.setOnMouseDragEntered(new MouseHandler() {
      @Override
      public void handle(MouseEvent event, int row, int column) {
        if (event.isPrimaryButtonDown()) {
          if (!select.isEmpty()) {
            drawTile(select.getSelectedRow(), select.getSelectedColumn(), Square.BLANK);
          }

          if (field.canReveal(row, column)) {
            drawTile(row, column, Square.EXPOSED);
            select.select(row, column);
          }
        }
      }
    });
  }

  private static abstract class MouseHandler implements EventHandler<MouseEvent> {
    @Override
    public void handle(MouseEvent e) {
      int row = (int) e.getY() / SQUAREH;
      int col = (int) e.getX() / SQUAREW;

      handle(e, row, col);
      e.consume();
    }

    public abstract void handle(MouseEvent t, int row, int column);
  }

  private void drawTile(int row, int column, Square square) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.drawImage(Tiles.getImage(square), column * SQUAREW, row * SQUAREH);
  }

  @Subscribe
  public void cellUpdated(SquareChangeEvent event) {
    int row = event.getRow();
    int col = event.getColumn();
    Square square = event.getSquare();

    if (square == Square.EXPOSED)
      drawNumber(row, col, field.getMineCount(row, col));
    else {
      GraphicsContext gc = canvas.getGraphicsContext2D();
      gc.drawImage(Tiles.getImage(square), col * SQUAREW, row * SQUAREH);
    }
  }

  @Subscribe
  public void fieldStateChanged(Minefield.State state) {
  }

  private void drawNumber(int row, int column, int number) {
    double xDest = column * SQUAREW;
    double yDest = row * SQUAREH;
    double xSrc = 0;
    double ySrc = number * SQUAREH;

    canvas.getGraphicsContext2D().drawImage(Tiles.NUMBERS, xSrc, ySrc, SQUAREW, SQUAREH, xDest,
        yDest, SQUAREW, SQUAREH);
  }

  @Subscribe
  public void updateBoard(BoardChangeEvent event) {
    int rows = field.getRowCount();
    int cols = field.getColumnCount();

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        Square square = field.getSquareAt(row, col);

        if (square == Square.EXPOSED)
          drawNumber(row, col, field.getMineCount(row, col));
        else
          canvas.getGraphicsContext2D().drawImage(Tiles.getImage(square), col * SQUAREW,
              row * SQUAREH);
      }
    }
  }
}
