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

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.foobar.minesweeper.model.Minefield;

/**
 * The application class for JavaFX Minesweeper
 *
 */
public class Minesweeper extends Application {
  private final Pane canvas = new Pane();
  private final ScrollPane sPane = new ScrollPane();
  private boolean spawnMode;

  public Minesweeper() {
  }

  @Override public void start(Stage stage) {
    Button button = new Button("New Minesweeper");

    button.setOnAction(this::onNewMinesweeper);

    sPane.setOnMouseClicked(this::onPaneClicked);

    HBox box = new HBox();
    box.setPadding(new Insets(15, 12, 15, 12));
    box.setStyle("-fx-background-color: #336699;");
    box.getChildren().add(button);

    BorderPane bpane = new BorderPane();
    sPane.setContent(canvas);
    sPane.setVbarPolicy(ScrollBarPolicy.NEVER);
    sPane.setHbarPolicy(ScrollBarPolicy.NEVER);

    bpane.setTop(box);
    bpane.setCenter(sPane);

    stage.setTitle("JavaFX Minesweeper");
    stage.setScene(new Scene(bpane, 600, 600));

    stage.show();
  }

  private void onPaneClicked(MouseEvent event) {
    if (!spawnMode)
      return;

    Minefield minefield = new Minefield(10, 10, 10);

    MinesweeperPane minesweeper = new MinesweeperPane(minefield, this);
    minesweeper.asParent().relocate(event.getX(), event.getY());
    minesweeper.asParent().requestFocus();

    canvas.getChildren().add(minesweeper.asParent());
    spawnMode = false;
    sPane.setCursor(Cursor.DEFAULT);
  }

  public void onClone(MinesweeperPane original) {
    double x = original.asParent().getLayoutX();
    double y = original.asParent().getLayoutY();

    MinesweeperPane cloned = new MinesweeperPane(original);
    cloned.asParent().setLayoutX(x + 20);
    cloned.asParent().setLayoutY(y + 20);
    canvas.getChildren().add(cloned.asParent());
  }

  public void onClose(MinesweeperPane toClose) {
    canvas.getChildren().remove(toClose.asParent());
  }

  private void onNewMinesweeper(ActionEvent event) {
    spawnMode = true;
    sPane.setCursor(Cursor.CROSSHAIR);
  }
}
