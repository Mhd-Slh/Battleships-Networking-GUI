# Battleships

A Java implementation of the classic Battleship game, combining a JavaFX graphical interface for ship placement with a socket-based client/server foundation for multiplayer play.
The project implements grid-based ship placement with rotation and validation, hit/miss/sunk game logic, and a threaded server that relays messages between two connected clients.

## Requirements

- JDK 17+
- JavaFX SDK 17+ (matching your JDK version)

Download the JavaFX SDK from [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/) and note the path to its `lib` folder.

## Running the Project

From the project directory, compile and run the placement GUI with the JavaFX SDK on the module path:

\`\`\`bash
javac --module-path "/path/to/javafx-sdk-XX/lib" --add-modules javafx.controls,javafx.fxml *.java
java --module-path "/path/to/javafx-sdk-XX/lib" --add-modules javafx.controls,javafx.fxml Main
\`\`\`

The networking layer can be run independently to test the client/server relay:

\`\`\`bash
java GameServer      # waits for two client connections
java GameClient      # run once per client, in separate terminals
\`\`\`

## Features

- Interactive 10x10 JavaFX grid for ship placement
- Left-click to place, right-click to rotate, with live green highlighting for valid placement
- Standard Battleship fleet (Carrier, Battleship, Cruiser, Submarine, Destroyer)
- Board logic for placement validation and hit/miss/sunk resolution
- Threaded socket server relaying messages between two connected clients

## In Progress

- Connecting GUI actions (placement, attacks) to the network layer
- Defining a message protocol for syncing game state between clients
- Attack-phase GUI and win/loss screen
