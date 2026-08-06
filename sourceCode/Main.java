import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage stage)
    {
        Scene startScene = createStartScene(stage);
        stage.setScene(startScene);
        stage.setTitle("Battleships");
        stage.show();
    }

    private Scene createStartScene(Stage stage)
    {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Battleships");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

        Button startButton = new Button("Start Game");
        startButton.setOnAction(e -> {
            Scene gameScene = createGameScene(stage);
            stage.setScene(gameScene);
        });

        root.getChildren().addAll(title, startButton);

        return new Scene(root, 800, 800);
    }

    // Needed to put this here for the lambda function
    Ship currentShip;
    boolean horizontal = false;
    Ship[] formation =  new Ship[5];

    private Scene createGameScene(Stage stage)
    {
        Board board = new Board();
        GridPane grid_pane = new GridPane();

        Rectangle[][] rects = new Rectangle[10][10];

        Ship carrier = new Ship(5);
        Ship battleship = new Ship(4);
        Ship cruiser = new Ship(3);
        Ship submarine = new Ship(3);
        Ship destroyer = new Ship(2);
        currentShip = carrier;

        // To be accessible in the for-loop
        Button saveFormation  = new Button("Save Formation");
        saveFormation.setDisable(true);

        for(int row = 0; row < 10; row++)
        {
            for(int col = 0; col < 10; col++)
            {
                Rectangle rect = new Rectangle(80, 80);
                rect.setStroke(Color.BLACK);

                updateCellColor(rect, board.getCell(row, col));

                rects[row][col] = rect;

                final int r = row;
                final int c = col;

                rect.setOnMouseClicked(e -> {
                    // Rotate ship
                    // While updating the highlighting
                    if (e.getButton() == MouseButton.SECONDARY)
                    {
                        horizontal = !horizontal;
                        resetGridColor(board, rects);
                        highlightPlacement(board, r, c, rects);
                    }

                    // Place ship
                    if (e.getButton() == MouseButton.PRIMARY)
                    {
                        if (board.canPlaceShip(currentShip, r, c, horizontal))
                        {
                            if (currentShip.isPlaced())
                            {
                                board.clearShip(currentShip);
                                resetGridColor(board, rects);
                            }
                            currentShip.place(r, c, horizontal);
                            currentShip.setPlaced(true);
                            board.placeShip(currentShip, r, c, horizontal);

                            resetGridColor(board, rects);
                        }

                        // Enables the button if all ships are placed
                        saveFormation.setDisable(!carrier.isPlaced() || !battleship.isPlaced() || !cruiser.isPlaced()
                                || !submarine.isPlaced() || !destroyer.isPlaced());
                    }
                });

                // Highlights the cell that the cursor is hovering light green
                // If it can place currentShip
                rect.setOnMouseEntered(e -> highlightPlacement(board, r, c, rects));

                // Resets the color of cells back to normal after the hovering stops
                rect.setOnMouseExited(e -> resetGridColor(board, rects));

                grid_pane.add(rect, col, row);
            }
        }

        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setAlignment(Pos.TOP_CENTER);

        Label shipLabel = new Label("Select Ship");
        shipLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label placeRotateLabel = new Label("Left Click to Place Ship" +
                                               "\nRight Click to Rotate Ship");
        placeRotateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnCarrier    = new Button("Carrier (5)");
        Button btnBattleship = new Button("Battleship (4)");
        Button btnCruiser    = new Button("Cruiser (3)");
        Button btnSubmarine  = new Button("Submarine (3)");
        Button btnDestroyer  = new Button("Destroyer (2)");
        // Had to move this button above the for-loop to be able to access it
        //Button saveFormation  = new Button("Save Formation");

        int btnWidth = 150;
        btnCarrier.setMinWidth(btnWidth);
        btnBattleship.setMinWidth(btnWidth);
        btnCruiser.setMinWidth(btnWidth);
        btnSubmarine.setMinWidth(btnWidth);
        btnDestroyer.setMinWidth(btnWidth);
        saveFormation.setMinWidth(btnWidth);

        btnCarrier.setOnAction(e -> currentShip = carrier);
        btnBattleship.setOnAction(e -> currentShip = battleship);
        btnCruiser.setOnAction(e -> currentShip = cruiser);
        btnSubmarine.setOnAction(e -> currentShip = submarine);
        btnDestroyer.setOnAction(e -> currentShip = destroyer);
        saveFormation.setOnAction(e -> {
            formation[0] = carrier;
            formation[1] = battleship;
            formation[2] = cruiser;
            formation[3] = submarine;
            formation[4] = destroyer;
        });

        leftPanel.getChildren().addAll(
                shipLabel,
                btnCarrier,
                btnBattleship,
                btnCruiser,
                btnSubmarine,
                btnDestroyer,
                placeRotateLabel,
                saveFormation
        );

        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(grid_pane);

        return new Scene(root, 1100, 850);
    }

    private void resetGridColor(Board board, Rectangle[][] rects)
    {
        for(int i = 0; i < 10; i++)
        {
            for(int j = 0; j < 10; j++)
            {
                updateCellColor(rects[i][j], board.getCell(i, j));
            }
        }
    }

    private void highlightPlacement(Board board, int r, int c, Rectangle[][] rects)
    {
        if (board.canPlaceShip(currentShip, r, c, horizontal))
        {
            for (int i = 0; i < currentShip.getLength(); i++)
            {
                int rr = r + (horizontal ? 0 : i);
                int cc = c + (horizontal ? i : 0);

                if (board.isInBounds(rr, cc))
                {
                    rects[rr][cc].setFill(Color.LIGHTGREEN);
                }
            }
        }
    }

    private void updateCellColor(Rectangle rect, Board.CellState type)
    {
        switch(type)
        {
            case WATER: rect.setFill(Color.LIGHTBLUE); break;
            case SHIP:  rect.setFill(Color.GRAY); break;
            case HIT:   rect.setFill(Color.RED); break;
            case MISS:  rect.setFill(Color.WHITE); break;
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}