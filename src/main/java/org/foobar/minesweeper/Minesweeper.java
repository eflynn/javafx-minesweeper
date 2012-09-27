/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foobar.minesweeper;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import static javafx.scene.input.MouseButton.*;
import javafx.scene.input.MouseEvent;
import static javafx.scene.input.MouseEvent.*;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;
import org.foobar.minesweeper.event.BoardChangeEvent;
import org.foobar.minesweeper.event.CellChangeEvent;
import org.foobar.minesweeper.model.Minefield;
import org.foobar.minesweeper.model.SquareType;
import static org.foobar.minesweeper.model.SquareType.BLANK;
import static org.foobar.minesweeper.model.SquareType.FLAG;
import static org.foobar.minesweeper.model.SquareType.HITMINE;
import static org.foobar.minesweeper.model.SquareType.MINE;
import static org.foobar.minesweeper.model.SquareType.WRONGMINE;

/**
 *
 * @author User
 */
public class Minesweeper extends Application {
    private static final int CELL = 24;
    private final Button newGameButton = new Button("New Game");
    private final Canvas canvas = new Canvas(240, 240);
    private final Image image = new Image("file:sprites.bmp");
    private final Parent root =
            VBoxBuilder.create().children(newGameButton, canvas).build();
    private final EventBus eventBus = new EventBus();
    private Minefield field = new Minefield(eventBus);
    private final GraphicsContext context = canvas.getGraphicsContext2D();
    private int lastRow = -1;
    private int lastCol = -1;

    public Minesweeper() {
        canvas.addEventHandler(MOUSE_PRESSED, new MouseHandler() {
            @Override
            public void handle(MouseEvent event, int row, int column) {
                if (event.isSecondaryButtonDown()) {
                    field.toggleFlag(row, column);
                }
                else if (event.isPrimaryButtonDown() && field.canReveal(row, column)) {
                    drawTile(row, column, 5);
                    setSelect(row, column);
                }
            }
        });

        canvas.addEventHandler(MOUSE_CLICKED, new MouseHandler() {
            @Override
            public void handle(MouseEvent e, int row, int column) {
                int clicks = e.getClickCount();
                MouseButton button = e.getButton();

                if ((clicks == 2 && button == PRIMARY) || button == MIDDLE) {
                    field.revealNearby(row, column);
                }
                else if (clicks == 1 && button == PRIMARY) {
                    clearSelect();
                    field.reveal(row, column);
                }

            }
        });

        canvas.addEventHandler(MOUSE_DRAGGED, new MouseHandler() {
            @Override
            public void handle(MouseEvent event, int row, int column) {
                if (event.isPrimaryButtonDown()) {
                    if (isSelected()) {
                        drawTile(lastRow, lastCol, 0);
                    }

                    if (field.canReveal(row, column)) {
                        drawTile(row, column, 5);
                        setSelect(row, column);
                    }
                }
            }
        });

        newGameButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new MouseHandler() {
            @Override
            public void handle(MouseEvent event, int row, int column) {
                field.restart();
            }
        });
    }

    private void clearSelect() {
        lastRow = -1;
        lastCol = -1;
    }

    private int getTile(SquareType type) {
        switch (type) {
        case BLANK:
            return 0;
        case FLAG:
            return 1;
        case MINE:
            return 2;
        case HITMINE:
            return 3;
        case WRONGMINE:
            return 4;
        default:
            throw new AssertionError("Unknown square type: " + type);
        }
    }

    private boolean isSelected() {
        return lastRow != -1;
    }

    private void setSelect(int row, int column) {
        lastRow = row;
        lastCol = column;
    }

    @Override
    public void start(Stage primaryStage) {
        eventBus.register(this);

        primaryStage.setScene(new Scene(root, 250, 260));
        primaryStage.setTitle("Minesweeper");
        primaryStage.show();

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                drawTile(r, c, 0);
            }
        }
    }

    private void drawTile(int row, int column, int tile) {
        context.drawImage(image,
                0, tile * CELL, CELL, CELL,
                column * CELL, row * CELL, CELL, CELL);
    }

    private static abstract class MouseHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent e) {
            int row = (int) e.getY() / CELL;
            int col = (int) e.getX() / CELL;

            handle(e, row, col);
            e.consume();
        }

        public abstract void handle(MouseEvent t, int row, int column);
    }

    @Subscribe
    public void updateBoard(BoardChangeEvent event) {
        int rows = field.getRowCount();
        int columns = field.getColumnCount();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                SquareType square = field.getSquareAt(r, c);

                int tile = square.hasMineCount()
                        ? square.getMineCount() + 5 : getTile(square);

                drawTile(r, c, tile);
            }
        }
    }

    @Subscribe
    public void cellUpdated(CellChangeEvent event) {
        int row = event.getRow();
        int col = event.getColumn();

        int tile = event.getCell().hasMineCount()
                ? event.getCell().getMineCount() + 5
                : getTile(event.getCell());

        drawTile(row, col, tile);
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
