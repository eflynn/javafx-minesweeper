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

import com.google.common.eventbus.EventBus;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.foobar.minesweeper.model.Minefield;

public class Minesweeper extends Application {
  private final EventBus eventBus = new EventBus();
  private final Minefield field = new Minefield(eventBus);

  public Minesweeper() {}

  @Override
  public void start(Stage stage) {
    Parent root = new MinesweeperPane(field, eventBus);
    eventBus.register(root);

    stage.setResizable(false);
    stage.setScene(new Scene(root, 260, 300));
    stage.setTitle("Minesweeper");
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
