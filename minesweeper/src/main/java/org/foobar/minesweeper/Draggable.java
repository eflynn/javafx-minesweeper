package org.foobar.minesweeper;

import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class Draggable {
  static class Delta {
    double x;
    double y;
  }

  public static void makeDraggable(final Node node) {
    node.setCacheHint(CacheHint.SPEED);

    final Delta dragDelta = new Delta();

    node.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        // record a delta distance for the drag and drop operation.
        dragDelta.x = mouseEvent.getX();
        dragDelta.y = mouseEvent.getY();
        node.toFront();
      }
    });
    node.setOnMouseDragged(new EventHandler<MouseEvent>() {
      @Override public void handle(MouseEvent mouseEvent) {
        node.relocate(mouseEvent.getSceneX() - dragDelta.x, mouseEvent.getSceneY() - dragDelta.y);
      }
    });
  }
}
