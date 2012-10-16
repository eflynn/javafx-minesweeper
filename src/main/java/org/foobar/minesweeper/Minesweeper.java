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
  
  public Minesweeper() {
  }

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
