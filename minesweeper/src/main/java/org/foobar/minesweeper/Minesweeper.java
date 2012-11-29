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
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.foobar.minesweeper.model.Minefield;

public class Minesweeper extends Application {
  private double nextX = 200.0;
  private double nextY = 200.0;

  public Minesweeper() {
  }

  @Override
  public void start(Stage stage) {
    Minefield field = new Minefield(10, 10, 10);

    showStage(stage, new MinesweeperPane(this, field));
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
