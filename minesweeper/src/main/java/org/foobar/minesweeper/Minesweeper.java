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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
  private double nextX = 200.0;
  private double nextY = 200.0;

  public Minesweeper() {
  }

  @Override public void start(Stage stage) {
    Pane group = new Pane();
    Minefield field = new Minefield(10, 10, 10);

    for(int i=0; i < 3; i++) {
      HasParent minesweeper = new MinesweeperPane(this, field);
      Parent node = minesweeper.asParent();
      Draggable.makeDraggable(node);

      node.setLayoutX(i * 400);

      group.getChildren().add(node);
    }

    Button button = new Button("New Minesweeper");
    HBox box = new HBox();
    box.setPadding(new Insets(15, 12, 15, 12));
    box.setStyle("-fx-background-color: #336699;");
    box.getChildren().add(button);
    BorderPane pane = new BorderPane();
    pane.setTop(box);
    pane.setCenter(group);

//    group.setStyle("-fx-fill: yellow;" +
//    "-fx-stroke: green;" +
//    "-fx-stroke-width: 5;" +
//    "-fx-stroke-dash-array: 12 2 4 2;" +
//    "-fx-stroke-dash-offset: 6;" +
//    "-fx-stroke-line-cap: butt;");
//    group.setAutoSizeChildren(false);

    stage.setTitle("JavaFX Minesweeper");
    stage.setScene(new Scene(pane, 600, 400));
    stage.show();
  }

  public void createAndShowStage(HasParent root) {
    showStage(new Stage(), root);
  }

  private void showStage(Stage stage, HasParent root) {
    stage.setX(nextX);
    stage.setY(nextY);
    nextX += 20.0;
    nextY += 20.0;

    stage.setResizable(false);
    stage.setTitle("Minesweeper");
    stage.setScene(new Scene(root.asParent()));
    stage.show();
  }
}
