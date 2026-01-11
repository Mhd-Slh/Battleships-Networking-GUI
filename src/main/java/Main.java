import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

public class Main extends Application
{
    private boolean readySent = false;
    private boolean gameStarted = false;
    private boolean placementLocked = false;
    private boolean gameOver = false;

    private boolean myFormationReady = false;
    private boolean enemyFormationReady = false;
    private Ship[] formation = new Ship[5];
    private Ship[] enemyFormation = new Ship[5];

    private Ship currentShip;
    private boolean horizontal = false;
    private boolean myTurn = false;
    private boolean isHost = false;

    private Board myBoard;
    private Board enemyBoard;
    private Rectangle[][] myRectangles = new Rectangle[10][10];
    private Rectangle[][] enemyRectangles = new Rectangle[10][10];

    private Ship carrier    = new Ship(5);
    private Ship battleship = new Ship(4);
    private Ship cruiser    = new Ship(3);
    private Ship submarine  = new Ship(3);
    private Ship destroyer  = new Ship(2);

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
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0a1a2f, #09203f);");

        Label title = new Label("Battleships");
        title.setStyle(
                "-fx-font-size: 48px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 5);"
        );

        Button hostButton = new Button("Host Game");
        Button connectButton = new Button("Connect to Game");
        String buttonStyle =
                "-fx-background-color: #1e88e5;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-padding: 12px 28px;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;";

        String buttonHoverStyle =
                "-fx-background-color: #42a5f5;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-padding: 12px 28px;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-cursor: hand;";

        hostButton.setStyle(buttonStyle);
        connectButton.setStyle(buttonStyle);

        hostButton.setOnMouseEntered(e -> hostButton.setStyle(buttonHoverStyle));
        hostButton.setOnMouseExited(e -> hostButton.setStyle(buttonStyle));
        connectButton.setOnMouseEntered(e -> connectButton.setStyle(buttonHoverStyle));
        connectButton.setOnMouseExited(e -> connectButton.setStyle(buttonStyle));

        hostButton.setOnAction(e -> showHostDialog(stage));
        connectButton.setOnAction(e -> showConnectDialog(stage));

        root.getChildren().addAll(title, hostButton, connectButton);

        return new Scene(root, 800, 800);
    }

    private void showHostDialog(Stage stage)
    {
        TextInputDialog dialog = new TextInputDialog("5000");
        dialog.setTitle("Host Game");
        dialog.setHeaderText("Enter port to host on:");
        dialog.setContentText("Port:");

        dialog.showAndWait().ifPresent(portStr -> {
            int port = Integer.parseInt(portStr);
            new Thread(() -> {
                try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0")))
                {
                    isHost = true;
                    Socket client = serverSocket.accept();
                    Network.connection = new Network(client, this::handleMessage);
                    Platform.runLater(() -> stage.setScene(createGameScene(stage)));
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    showError("Failed to host: " + ex.getMessage());
                }
            }).start();
        });
    }

    private void showConnectDialog(Stage stage)
    {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Connect to Game");

        TextField ipField = new TextField("127.0.0.1");
        TextField portField = new TextField("5000");

        VBox vbox = new VBox(10, new Label("IP:"), ipField, new Label("Port:"), portField);
        vbox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            new Thread(() -> {
                try
                {
                    Socket socket = new Socket(ip, port);
                    isHost = false;
                    Network.connection = new Network(socket, this::handleMessage);
                    Platform.runLater(() -> stage.setScene(createGameScene(stage)));
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                    showError("Failed to connect: " + ex.getMessage());
                }
            }).start();
        }
    }

    private Scene createGameScene(Stage stage)
    {
        myBoard = new Board();
        enemyBoard = new Board();

        GridPane myGrid = new GridPane();
        GridPane enemyGrid = new GridPane();
        myGrid.setHgap(1);
        myGrid.setVgap(1);
        enemyGrid.setHgap(1);
        enemyGrid.setVgap(1);

        for (int row = 0; row < 10; row++)
        {
            for (int col = 0; col < 10; col++)
            {
                Rectangle r1 = new Rectangle(40, 40);
                r1.setStroke(Color.BLACK);
                updateCellColor(r1, myBoard.getCell(row, col));
                final int rr = row;
                final int cc = col;

                r1.setOnMouseClicked(e -> {
                    if (placementLocked) return;
                    if (currentShip == null) return;

                    if (e.getButton() == MouseButton.SECONDARY)
                    {
                        horizontal = !horizontal;
                        resetGridColor(myBoard, myRectangles);
                        highlightPlacement(myBoard, rr, cc, myRectangles);
                    }
                    else if (e.getButton() == MouseButton.PRIMARY)
                    {
                        if (myBoard.canPlaceShip(currentShip, rr, cc, horizontal))
                        {
                            if (currentShip.isPlaced())
                            {
                                myBoard.clearShip(currentShip);
                                resetGridColor(myBoard, myRectangles);
                            }
                            currentShip.place(rr, cc, horizontal);
                            currentShip.setPlaced(true);
                            myBoard.placeShip(currentShip, rr, cc, horizontal);
                            resetGridColor(myBoard, myRectangles);
                        }
                    }
                });

                r1.setOnMouseEntered(e -> { if (!placementLocked) highlightPlacement(myBoard, rr, cc, myRectangles); });
                r1.setOnMouseExited(e -> { if (!placementLocked) resetGridColor(myBoard, myRectangles); });
                myRectangles[row][col] = r1;
                myGrid.add(r1, col, row);

                Rectangle r2 = new Rectangle(40, 40);
                r2.setStroke(Color.BLACK);
                r2.setFill(Color.LIGHTBLUE);
                enemyRectangles[row][col] = r2;
                final int er = row;
                final int ec = col;

                r2.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY)
                    {
                        if (!myTurn || gameOver) return;
                        Board.CellState s = enemyBoard.getCell(er, ec);
                        if (s == Board.CellState.HIT || s == Board.CellState.MISS) return;

                        if (Network.connection != null)
                        {
                            Network.connection.send("SHOT " + er + " " + ec);
                            enemyRectangles[er][ec].setFill(Color.GRAY);
                            myTurn = false;
                        }
                        else
                        {
                            showError("Not connected to opponent.");
                        }
                    }
                });

                enemyGrid.add(r2, col, row);
            }
        }

        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setAlignment(Pos.TOP_CENTER);

        Label shipLabel = new Label("Select Ship");
        shipLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label instructions = new Label("Left Click to Place Ship\nRight Click to Rotate");

        Button btnCarrier    = new Button("Carrier (5)");
        Button btnBattleship = new Button("Battleship (4)");
        Button btnCruiser    = new Button("Cruiser (3)");
        Button btnSubmarine  = new Button("Submarine (3)");
        Button btnDestroyer  = new Button("Destroyer (2)");
        Button saveFormation = new Button("Save Formation");
        saveFormation.setDisable(true);

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


        //debug btn
        Button debugButton = new Button("Debug");
        debugButton.setMinWidth(btnWidth);
        debugButton.setOnAction(e -> openDebugMenu());


        saveFormation.setOnAction(e -> {
            if (placementLocked) return;

            formation[0] = carrier;
            formation[1] = battleship;
            formation[2] = cruiser;
            formation[3] = submarine;
            formation[4] = destroyer;

            placementLocked = true;

            btnCarrier.setDisable(true);
            btnBattleship.setDisable(true);
            btnCruiser.setDisable(true);
            btnSubmarine.setDisable(true);
            btnDestroyer.setDisable(true);
            saveFormation.setDisable(true);

            if (Network.connection != null)
            {
                Network.connection.send("FORMATION " + serializeFormation());
                myFormationReady = true;

                if (!readySent)
                {
                    Network.connection.send("READY");
                    readySent = true;
                }

                checkBothReady();
            } else {
                showError("Not connected.");
            }
        });

        final Runnable enableChecker = () -> saveFormation.setDisable(
                !(carrier.isPlaced() && battleship.isPlaced() && cruiser.isPlaced() && submarine.isPlaced() && destroyer.isPlaced()) || placementLocked
        );
        enableChecker.run();

        leftPanel.getChildren().addAll(shipLabel, btnCarrier, btnBattleship, btnCruiser, btnSubmarine, btnDestroyer, instructions, saveFormation,debugButton);

        HBox center = new HBox(20);
        VBox myBox = new VBox(10, new Label("Your Board"), myGrid);
        VBox enemyBox = new VBox(10, new Label("Enemy Board"), enemyGrid);
        myBox.setAlignment(Pos.CENTER);
        enemyBox.setAlignment(Pos.CENTER);
        center.getChildren().addAll(myBox, enemyBox);
        center.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setLeft(leftPanel);
        root.setCenter(center);

        new Thread(() -> {
            try
            {
                while(true)
                {
                    Platform.runLater(enableChecker);
                    Thread.sleep(200);
                }
            } catch (InterruptedException ignored) {}
        }).start();

        return new Scene(root, 1100, 850);
    }

    private void handleMessage(String msg)
    {
        System.out.println("Received >>> " + msg);
        if (msg.startsWith("FORMATION "))
        {
            onEnemyFormation(msg.substring(10));
        }
        else if (msg.equals("READY"))
        {
            onEnemyReady();
        }
        else if (msg.startsWith("SHOT "))
        {
            onEnemyShot(msg);
        }
        else if (msg.startsWith("RESULT "))
        {
            onShotResult(msg);
        }
        else if (msg.startsWith("GAME OVER"))
        {
            onGameOverFromNetwork();
        }
        else
        {
            System.out.println("Unknown message: " + msg);
        }
    }

    private void onEnemyFormation(String data)
    {
        enemyFormation = parseFormation(data);

        for (Ship s : enemyFormation) {
            if (s == null || !s.isPlaced()) continue;

            for (int i = 0; i < s.getLength(); i++) {
                int rr = s.getRow() + (s.isHorizontal() ? 0 : i);
                int cc = s.getCol() + (s.isHorizontal() ? i : 0);
                enemyBoard.setOwnerOnly(rr, cc, s);
            }
        }

        enemyFormationReady = true;
        checkBothReady();
    }

    private void onEnemyReady()
    {
        enemyFormationReady = true;
        checkBothReady();
    }

    private void checkBothReady()
    {
        if (myFormationReady && enemyFormationReady && !gameStarted)
        {
            startGameTurns();
        }
    }

    private void startGameTurns()
    {
        if (gameStarted) return;
        gameStarted = true;

        myTurn = isHost;
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Both players ready. Game starts! Your turn: " + myTurn, ButtonType.OK);
            a.show();
        });
    }

    private void onEnemyShot(String msg)
    {
        String[] p = msg.split(" ");
        int r = Integer.parseInt(p[1]);
        int c = Integer.parseInt(p[2]);

        boolean hit = myBoard.shoot(r, c);
        Platform.runLater(() -> updateCellColor(myRectangles[r][c], myBoard.getCell(r, c)));

        if (Network.connection != null)
        {
            Network.connection.send("RESULT " + r + " " + c + " " + hit);
        }
        if (!hit && !gameOver) { myTurn = true; }
    }

    private void onShotResult(String msg)
    {
        String[] parts = msg.split(" ");
        if (parts.length >= 4)
        {
            int r = Integer.parseInt(parts[1]);
            int c = Integer.parseInt(parts[2]);
            boolean isHit = Boolean.parseBoolean(parts[3]);
            Platform.runLater(() -> updateCellColor(enemyRectangles[r][c], enemyBoard.getCell(r, c)));

            if (isHit)
            {
                if (!gameOver) { myTurn = true; }

                enemyBoard.setCell(r, c, Board.CellState.HIT);
                Ship s = enemyBoard.getOwnerAt(r, c);
                if (s != null)
                {
                    s.registerHit(r, c);
                    if (s.isSunk()) Platform.runLater(() -> markShipAsSunk(s));
                }
                if (allShipsSunk(enemyFormation) && !gameOver)
                {
                    gameOver = true;
                    if (Network.connection != null)
                    {
                        Network.connection.send("GAME OVER");
                    }
                    Platform.runLater(() -> { Alert a = new Alert(Alert.AlertType.INFORMATION,
                            "You sank all enemy ships. You WIN!", ButtonType.OK);
                        a.show();
                    });
                }
            }
            else
            {
                if (!gameOver) { myTurn = false; }

                enemyBoard.setCell(r, c, Board.CellState.MISS);
            }
        }
    }

    private void onGameOverFromNetwork()
    {
        if (gameOver) return;

        gameOver = true;
        Platform.runLater(() -> { Alert a = new Alert(Alert.AlertType.INFORMATION,
                "All your ships have been sunk. You LOSE.", ButtonType.OK);
            a.show();
        });
    }

    private String serializeFormation()
    {
        StringBuilder sb = new StringBuilder();
        for (Ship s : formation)
        {
            if (s == null || !s.isPlaced())
            {
                continue;
            }
            sb.append(s.getRow()).append(",")
                    .append(s.getCol()).append(",")
                    .append(s.getLength()).append(",")
                    .append(s.isHorizontal()).append(";");
        }
        return sb.toString();
    }

    private Ship[] parseFormation(String data)
    {
        Ship[] ships = new Ship[5];
        String[] parts = data.split(";");
        int count = Math.min(parts.length, 5);
        for (int i = 0; i < count; i++)
        {
            if (parts[i].isEmpty()) continue;

            String[] sdata = parts[i].split(",");
            int row = Integer.parseInt(sdata[0]);
            int col = Integer.parseInt(sdata[1]);
            int length = Integer.parseInt(sdata[2]);
            boolean horizontal = Boolean.parseBoolean(sdata[3]);

            Ship s = new Ship(length);
            s.place(row, col, horizontal);
            s.setPlaced(true);
            ships[i] = s;
        }
        return ships;
    }

    private void resetGridColor(Board board, Rectangle[][] rects)
    {
        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                updateCellColor(rects[i][j], board.getCell(i, j));
            }
        }
    }

    private void highlightPlacement(Board board, int r, int c, Rectangle[][] rects)
    {
        if (currentShip == null) return;

        if (board.canPlaceShip(currentShip, r, c, horizontal))
        {
            for (int i = 0; i < currentShip.getLength(); i++)
            {
                int rr = r + (horizontal ? 0 : i);
                int cc = c + (horizontal ? i : 0);
                if (board.isInBounds(rr, cc)) rects[rr][cc].setFill(Color.LIGHTGREEN);
            }
        }
    }

    private void updateCellColor(Rectangle rect, Board.CellState type)
    {
        switch (type)
        {
            case WATER: rect.setFill(Color.LIGHTBLUE); break;
            case SHIP:  rect.setFill(Color.GRAY); break;
            case HIT:   rect.setFill(Color.RED); break;
            case MISS:  rect.setFill(Color.WHITE); break;
        }
    }

    private void showError(String msg)
    {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait());
    }

    private void markShipAsSunk(Ship ship)
    {
        for (int i = 0; i < ship.getLength(); i++)
        {
            int rr = ship.getRow() + (ship.isHorizontal() ? 0 : i);
            int cc = ship.getCol() + (ship.isHorizontal() ? i : 0);
            enemyRectangles[rr][cc].setFill(Color.BLACK);
        }
    }

    private boolean allShipsSunk(Ship[] ships)
    {
        if (ships == null) return false;
        for (Ship s : ships) { if (s != null && !s.isSunk()) return false; }
        return true;
    }

    private void openDebugMenu()
    {
        Stage debugStage = new Stage();
        debugStage.setTitle("Debug");

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        Button sinkAllBtn = new Button("Sink All Ships");
        Button checkSunkBtn = new Button("Check If Ships Are Sunk");

        sinkAllBtn.setOnAction(e ->{
            if (!placementLocked){
                System.out.println("place ships");
                return;
            }

            for (Ship s : formation){
                if (s == null || !s.isPlaced()) continue;

                int len = s.getLength();
                int r0 = s.getRow();
                int c0 = s.getCol();

                for (int i = 0; i < len; i++){
                    int r = r0 + (s.isHorizontal() ? 0 : i);
                    int c = c0 + (s.isHorizontal() ? i : 0);

                    myBoard.shoot(r, c);
                    updateCellColor(myRectangles[r][c], myBoard.getCell(r, c));
                }
            }
        });

        checkSunkBtn.setOnAction(e ->{
            boolean all = true;
            for (Ship s : formation) {
                if (s != null && !s.isSunk()) {
                    all = false;
                }
            }
            System.out.println("All my ships sunk: " + all);
        });

        root.getChildren().addAll(sinkAllBtn, checkSunkBtn);

        Scene scene = new Scene(root, 220, 140);
        debugStage.setScene(scene);
        debugStage.show();
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
