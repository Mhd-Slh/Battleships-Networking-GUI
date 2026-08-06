# Battleships

A Java implementation of the classic Battleship game with real-time multiplayer over a network, built with a JavaFX interface for ship placement and combat.
The project implements ship placement with rotation and validation, a custom TCP message protocol for syncing formations and shots between players, and turn-based hit/miss/sunk resolution with win/loss detection.

## Requirements

- JDK 17+
- JavaFX SDK 17+ (matching your JDK version)

Download the JavaFX SDK from [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/) and note the path to its `lib` folder.

## Running the Project

From the project directory, compile and run with the JavaFX SDK on the module path:

```bash
javac --module-path "/path/to/javafx-sdk-XX/lib" --add-modules javafx.controls,javafx.fxml *.java
java --module-path "/path/to/javafx-sdk-XX/lib" --add-modules javafx.controls,javafx.fxml Main
```

To play a match, run the program on two machines (or two terminals on the same machine):

- On one instance, click **Host Game** and enter a port to listen on.
- On the other, click **Connect to Game** and enter the host's IP address and port.

Once both players place and save their ships, the game starts automatically.

## Features

- Interactive 10x10 JavaFX grids for ship placement and combat, with click-to-place, right-click rotation, and live green highlighting for valid placement
- Host/connect networking over TCP sockets, with a background listener thread parsing incoming messages
- Custom text-based message protocol (`FORMATION`, `READY`, `SHOT`, `RESULT`, `GAME OVER`) for syncing game state between players
- Turn-based combat with hit/miss/sunk resolution, synced live to both players' boards
- Win/loss detection with an end-of-game alert
- Debug menu for quickly sinking all ships or checking sunk status during testing
