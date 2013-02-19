Minesweeper-Teacher
===================

# MainActivity.java
Main menu
* Start game
* Set settings

## Game.java
Game container
* Contains actual game object
* Updates game via input from external buttons
  * New game
  * Hint/guess
  * Scroll (temporary)

### GameView.java
Main game object
* Implements game logic
* Responds to clicks
* Responds to external actions (hints)
* Updates external data fields
  * Time
  * Remaining tile count
  * Mine count
* Draws game

#### BitmapCache.java
* Caches bitmaps

#### GameThread.java
* Refreshes and draws grid (probably inefficient)

#### Grid.java
Tile container

#### HintGrid.java
Extends game objects with hint detection capabilities
* Reveals surrounding tiles if appropriate number of surrounding flags have been placed
* Flags surrounding tiles if appropriate number of revealed tiles have been found
* Counts how many mines are in the shared and nonshared neighbors
* Repeat with multiple neighbors (TODO)

##### TileAction.java
Hint action container

#### Pair.java
Generic pair object

#### Tile.java
State of each tile

### HintDialog.java
* Prints hint text
* Allows you to perform the hint or do it yourself

### Timer.java
* Tracks time elapsed
* Update time field

### VictoryDialog.java
Victory dialog
