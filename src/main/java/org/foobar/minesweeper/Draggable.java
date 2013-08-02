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

import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class Draggable {
  private static class MouseHandler {
    private final Node node;
    private double offsetX;
    private double offsetY;

    MouseHandler(Node node) {
      this.node = node;
    }

    private void mouseOn(MouseEvent event) {
      offsetX = event.getSceneX() - node.getLayoutX();
      offsetY = event.getSceneY() - node.getLayoutY();
      node.toFront();
    }

    private void mouseDragged(MouseEvent event) {
      node.setLayoutX(event.getSceneX() - offsetX);
      node.setLayoutY(event.getSceneY() - offsetY);
    }
  }

  public static void makeDraggable(Node node) {
    node.setCache(true);
    node.setCacheHint(CacheHint.SPEED);

    final MouseHandler handler = new MouseHandler(node);

    node.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent event) {
        handler.mouseOn(event);
      }
    });
    node.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent event) {
        handler.mouseDragged(event);
      }
    });
  }
}
