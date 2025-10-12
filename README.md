# chess-engine-wrapper

A Clojure library that wraps UCI (Universal Chess Interface) standard chess engines to answer simple chess queries. Uses Stockfish by default but supports any UCI-compliant chess engine.

## Features

### Chess Engine Features
- Get all valid FEN positions reachable from a given position in one move
- Evaluate chess positions with configurable time limits
- UCI engine communication abstraction
- Default Stockfish integration with support for other UCI engines
- **Persistent engine sessions** for efficient multiple operations (2-3x faster than ephemeral sessions)

### Display Features
- Generate SVG checkerboards of any width × height dimensions
- Place chess pieces on boards using Unicode symbols
- Convert FEN notation to piece positions
- Calculate average material value from FEN positions
- Calculate material balance in pawn units
- Determine captured pieces for each side
- Fully responsive SVG output with CSS-based color theming
- Generate complete HTML pages with embedded boards

### General
- Simple, functional API
- Pure Clojure implementation

## Requirements

- Java 8 or higher
- Clojure 1.11.1 or higher
- A UCI-compliant chess engine (Stockfish recommended) - only needed for engine features

## Installation

### Installing Stockfish

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get install stockfish
```

**macOS:**
```bash
brew install stockfish
```

**Windows:**
Download from [Stockfish official website](https://stockfishchess.org/download/)

### Adding to your project

Add the library to your `deps.edn` by referencing a specific release tag:

```clojure
{:deps {io.github.arachtivix/chess-engine-wrapper 
        {:git/tag "v0.1.1" :git/sha "92a6690"}}}
```

Or use the latest commit from the main branch:

```clojure
{:deps {io.github.arachtivix/chess-engine-wrapper 
        {:git/url "https://github.com/arachtivix/chess-engine-wrapper" 
         :git/sha "92a6690f088638578142dacedbacbf0baaea4153"}}}
```

You can find the latest release tag and SHA on the [releases page](https://github.com/arachtivix/chess-engine-wrapper/releases).

Alternatively, you can use a local checkout:

```clojure
{:deps {chess-engine-wrapper {:local/root "/path/to/chess-engine-wrapper"}}}
```

## Usage

### Engine Features

#### Basic Usage

```clojure
(require '[chess-engine-wrapper.core :as chess])

;; Get all positions reachable from the starting position
(def starting-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
(def next-positions (chess/get-next-positions starting-fen))

;; Returns a vector of 20 FEN strings (one for each legal opening move)
(count next-positions) ;=> 20

;; Evaluate a position with a 1000ms time limit
(def evaluation (chess/get-position-value starting-fen 1000))
;=> {:score-cp 38, :best-move "e2e4"}
;; score-cp is in centipawns (100 = 1 pawn advantage for white)
;; Positive scores favor white, negative scores favor black
```

### Display Features

#### Generate SVG Checkerboards

```clojure
(require '[chess-engine-wrapper.display :as display])

;; 8x8 chess board with dark top-left square
(display/checkerboard 8 8 :dark)
;=> "<svg width=\"100%\" height=\"100%\" viewBox=\"0 0 400 400\"...>"

;; 10x10 checkers board with light top-left square
(display/checkerboard 10 10 :light)
```

#### Display Chess Pieces

```clojure
;; Standard chess starting position
(display/checkerboard-with-pieces 8 8 :dark (display/standard-chess-position))

;; Custom piece placement - positions are [row col] with [0 0] being top-left
(display/checkerboard-with-pieces 8 8 :dark {[3 3] :white-queen
                                              [3 4] :black-king
                                              [4 3] :black-knight
                                              [4 4] :white-bishop})
```

Available piece keywords:
- White pieces: `:white-king`, `:white-queen`, `:white-rook`, `:white-bishop`, `:white-knight`, `:white-pawn`
- Black pieces: `:black-king`, `:black-queen`, `:black-rook`, `:black-bishop`, `:black-knight`, `:black-pawn`

#### Convert FEN to Pieces

```clojure
;; Standard starting position
(display/fen->pieces "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
;; Returns {[0 0] :black-rook, [0 1] :black-knight, ...}

;; Just piece placement (without game state)
(display/fen->pieces "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")

;; Custom position
(display/fen->pieces "4k3/8/8/3Q4/8/8/8/4K3")
;; Returns {[0 4] :black-king, [3 3] :white-queen, [7 4] :white-king}
```

#### Calculate Material Value

```clojure
;; Standard starting position (78 total value / 32 pieces = 2.4375)
(display/fen->avg-material-value "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")
;=> 2.4375

;; Custom position
(display/fen->avg-material-value "4k3/8/8/3Q4/8/8/8/4K3")
;=> 3.0
```

Material values used: Pawn=1, Knight=3, Bishop=3, Rook=5, Queen=9, King=0

#### Calculate Material Balance

```clojure
;; Standard starting position (balanced)
(display/fen->material-balance "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")
;=> 0

;; Position where white is up a rook
(display/fen->material-balance "rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")
;=> 5

;; Position where black is up a queen
(display/fen->material-balance "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR")
;=> -9
```

#### Get Captured Pieces

```clojure
;; Standard starting position (no captures)
(display/fen->captured-pieces "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")
;=> {:white {} :black {}}

;; Position where white has lost a rook
(display/fen->captured-pieces "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1")
;=> {:white {:rook 1} :black {}}

;; Position where both sides have lost pieces
(display/fen->captured-pieces "r1bqkb1r/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R")
;=> {:white {} :black {:knight 1}}
```

Material values used: Pawn=1, Knight=3, Bishop=3, Rook=5, Queen=9, King=0

#### Generate FEN Position Display

Generate a complete HTML page with a chess position and detailed analysis:

```clojure
;; Standard starting position with full analysis (uses default 1000ms evaluation time)
(display/fen->html-display "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

;; Middlegame position with custom evaluation time (2000ms)
(display/fen->html-display "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4" 2000)

;; With custom engine path
(display/fen->html-display "4k3/8/8/3Q4/8/8/8/4K3" 1000 "/path/to/engine")
```

The generated HTML includes:
- SVG chessboard with the position
- FEN string display
- Active color and castling rights (if provided)
- En passant square (if applicable)
- Halfmove clock and fullmove number (if provided)
- Total piece count
- Material values for both sides
- Material balance with visual indicators (green for ahead, red for behind)
- **Engine evaluation** (in pawns, from white's perspective) with color-coded indicators
- List of captured pieces for each side

The page is fully responsive with a side-by-side layout on larger screens and stacked layout on mobile.

#### Generate Complete HTML Pages

```clojure
;; With default colors (chess green/cream)
(display/render-checkerboard-html 8 8 :dark)

;; With custom colors
(display/render-checkerboard-html 8 8 :dark "#b58863" "#f0d9b5")
```

The SVG uses CSS classes for styling, making it easy to change colors:

```css
.dark-square { fill: #769656; }  /* Dark squares */
.light-square { fill: #eeeed2; } /* Light squares */
.chess-piece { fill: #000; }     /* Chess piece color */
```

#### Running Display Examples

Generate example HTML files demonstrating all display features:

```bash
clojure -M -m example-display
```

This creates six HTML files:
- `example-chess.html` - 8×8 standard chess board
- `example-checkers.html` - 10×10 checkers board
- `example-custom-colors.html` - 5×5 board with custom colors
- `example-standard-position.html` - Board with standard chess starting position
- `example-custom-pieces.html` - Board with custom piece placement
- `example-fen-position.html` - Board showing a position from FEN notation

Generate FEN position display examples:

```bash
clojure -M -m example-fen-display
```

This creates ten HTML files demonstrating the `fen->html-display` function with various positions:
- `fen-display-starting-position.html` - Standard starting position with full analysis
- `fen-display-italian-game.html` - Italian Game opening position
- `fen-display-white-ahead.html` - Position where white is ahead in material
- `fen-display-black-ahead.html` - Position where black is ahead in material
- `fen-display-endgame.html` - Pawn endgame position
- `fen-display-en-passant.html` - Position with en passant opportunity
- `fen-display-castling.html` - Position showing castling rights
- `fen-display-simple-endgame.html` - Simple king and queen endgame
- `fen-display-middlegame.html` - Complex middlegame position
- `fen-display-many-captures.html` - Position with many captured pieces

### Using a Different Engine

```clojure
;; Use a custom engine path
(chess/get-next-positions starting-fen "/path/to/your/uci-engine")

;; Or set the default engine globally
(binding [chess/*default-engine-path* "/usr/local/bin/my-engine"]
  (chess/get-next-positions starting-fen))
```

### Advanced Usage with Engine Control

For more control over the engine lifecycle:

```clojure
(require '[chess-engine-wrapper.core :as chess]
         '[chess-engine-wrapper.uci :as uci])

;; Use the with-engine helper for custom operations
(chess/with-engine "stockfish"
  (fn [engine]
    (uci/set-position engine "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
    (uci/get-legal-moves engine)))
;=> ["a2a3" "b2b3" "c2c3" ... "g1h3"]
```

### Using Persistent Engine Sessions

**New in this version:** All engine functions can now accept either an engine path (string) or an initialized engine instance (map). This allows you to maintain a persistent engine session across multiple operations, which is significantly more efficient than creating a new engine instance for each operation.

#### Benefits of Persistent Sessions
- **Performance**: Avoid engine startup/shutdown overhead (typically 2-3x faster for multiple operations)
- **State preservation**: Engine maintains analysis data between requests
- **Resource efficiency**: Single engine process for multiple operations

#### Basic Persistent Session Usage

```clojure
(require '[chess-engine-wrapper.core :as chess])

;; Perform multiple operations with a single engine session
(chess/with-engine "stockfish"
  (fn [engine]
    ;; All these operations reuse the same engine instance
    (let [start-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          after-e4 "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"]
      
      ;; Get next positions using persistent session
      (chess/get-next-positions start-fen engine)
      
      ;; Evaluate position using persistent session
      (chess/get-position-value after-e4 1000 engine)
      
      ;; Continue using the same engine for more operations...
      )))
```

#### Analyzing Game Progressions

Persistent sessions are ideal for analyzing game progressions:

```clojure
(chess/with-engine "stockfish"
  (fn [engine]
    (let [game-positions [
           "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"      ; Start
           "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"   ; 1.e4
           "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2" ; 1...e5
           ]]
      ;; Analyze each position with the same engine
      (doseq [fen game-positions]
        (let [eval (chess/get-position-value fen 500 engine)]
          (println "Score:" (:score-cp eval) "Best move:" (:best-move eval)))))))
```

#### Backward Compatibility

The API remains fully backward compatible. Passing a string path still works as before:

```clojure
;; Old style - creates ephemeral engine session (still supported)
(chess/get-next-positions fen "stockfish")
(chess/get-position-value fen 1000 "stockfish")

;; New style - uses persistent engine session (more efficient)
(chess/with-engine "stockfish"
  (fn [engine]
    (chess/get-next-positions fen engine)
    (chess/get-position-value fen 1000 engine)))
```

#### Running the Persistent Session Example

See a complete demonstration of persistent sessions:

```bash
clojure -M -m example-persistent-session
```

This example demonstrates:
- Multiple operations with a single engine session
- Analyzing game progressions efficiently
- Direct UCI access within a session
- Performance comparison between persistent and ephemeral sessions

## API Reference

### Engine API

#### `get-next-positions`

```clojure
(get-next-positions fen)
(get-next-positions fen engine-or-path)
```

Given a FEN position string, returns all valid FEN positions reachable by one legal move.

**Parameters:**
- `fen`: A chess position in FEN (Forsyth-Edwards Notation) format
- `engine-or-path`: (optional) Either:
  - A string path to UCI engine executable (defaults to "stockfish") - creates ephemeral session
  - An initialized engine instance (map with :process, :in, :out) - uses persistent session

**Returns:**
A vector of FEN strings representing all positions reachable by one legal move.

**Example:**
```clojure
;; Ephemeral session (backward compatible)
(get-next-positions "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
;=> ["rnbqkbnr/1ppppppp/p7/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
;    "rnbqkbnr/p1pppppp/1p6/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
;    ...]

;; Persistent session (more efficient for multiple operations)
(with-engine "stockfish"
  (fn [engine]
    (get-next-positions "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" engine)))
```

#### `with-engine`

```clojure
(with-engine engine-path f)
```

Execute a function with an initialized engine. The function receives the engine as an argument.

**Parameters:**
- `engine-path`: Path to UCI engine executable
- `f`: Function that takes an engine and performs operations

**Returns:**
The result of calling function `f`

#### `get-position-value`

```clojure
(get-position-value fen movetime-ms)
(get-position-value fen movetime-ms engine-or-path)
```

Get the evaluation of a chess position given in FEN notation.

**Parameters:**
- `fen`: A chess position in FEN notation
- `movetime-ms`: Time limit for computation in milliseconds
- `engine-or-path`: (optional) Either:
  - A string path to UCI engine executable (defaults to "stockfish") - creates ephemeral session
  - An initialized engine instance (map with :process, :in, :out) - uses persistent session

**Returns:**
A map with `:score-cp` (centipawn score from white's perspective) and `:best-move`.
- A positive score favors white, negative favors black
- Score is in centipawns (100 = 1 pawn advantage)
- Returns nil if no evaluation is available

**Example:**
```clojure
;; Ephemeral session (backward compatible)
(get-position-value "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" 1000)
;=> {:score-cp 38, :best-move "e2e4"}

;; Persistent session (more efficient for multiple operations)
(with-engine "stockfish"
  (fn [engine]
    (get-position-value "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" 1000 engine)))
;=> {:score-cp 38, :best-move "e2e4"}
```

### Display API

#### `checkerboard`

```clojure
(checkerboard width height top-left-color)
(checkerboard width height top-left-color square-size)
```

Generate an HTML SVG checkerboard.

**Parameters:**
- `width` - Number of squares wide (integer)
- `height` - Number of squares tall (integer)  
- `top-left-color` - `:light` or `:dark` - determines the color of the top-left square
- `square-size` - (optional) Size of each square in pixels (default: 50)

**Returns:** SVG string with responsive viewBox

#### `checkerboard-with-pieces`

```clojure
(checkerboard-with-pieces width height top-left-color pieces)
(checkerboard-with-pieces width height top-left-color pieces square-size)
```

Generate an HTML SVG checkerboard with chess pieces.

**Parameters:**
- `width` - Number of squares wide (integer)
- `height` - Number of squares tall (integer)
- `top-left-color` - `:light` or `:dark`
- `pieces` - Map of positions to piece keywords, e.g. `{[0 0] :white-rook [0 1] :white-knight}`
- `square-size` - (optional) Size of each square in pixels (default: 50)

**Returns:** SVG string with chess pieces

#### `standard-chess-position`

```clojure
(standard-chess-position)
```

**Returns:** Map of the standard chess starting position with all 32 pieces

#### `fen->pieces`

```clojure
(fen->pieces fen)
```

Convert FEN (Forsyth-Edwards Notation) to pieces map format.

**Parameters:**
- `fen` - FEN string (can be full FEN or just the piece placement part)

**Returns:** Map of `[row col]` -> piece keyword

FEN notation describes positions from rank 8 (row 0) to rank 1 (row 7). Uppercase letters are white pieces, lowercase are black. Numbers indicate empty squares.

#### `fen->avg-material-value`

```clojure
(fen->avg-material-value fen)
```

Calculate average material value from a FEN position.

**Parameters:**
- `fen` - FEN string (can be full FEN or just the piece placement part)

**Returns:** Average material value as a double

Material values: Pawn=1, Knight=3, Bishop=3, Rook=5, Queen=9, King=0 (invaluable)

#### `fen->material-balance`

```clojure
(fen->material-balance fen)
```

Calculate the material balance from a FEN position in pawns worth.

**Parameters:**
- `fen` - FEN string (can be full FEN or just the piece placement part)

**Returns:** Material balance as an integer (in pawn units)

Returns the difference between white's material and black's material. Positive values indicate white is ahead, negative values indicate black is ahead. Kings have a value of 0 and do not affect the balance.

**Example:**
```clojure
(fen->material-balance "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")
;=> 0  ; balanced position

(fen->material-balance "rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")
;=> 5  ; white is ahead by a rook
```

#### `fen->captured-pieces`

```clojure
(fen->captured-pieces fen)
```

Determine which pieces have been captured based on a FEN position.

**Parameters:**
- `fen` - FEN string (can be full FEN or just the piece placement part)

**Returns:** Map with `:white` and `:black` keys containing maps of captured piece types to counts

Compares the current position to the standard starting position to identify missing pieces for each side.

**Example:**
```clojure
(fen->captured-pieces "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1")
;=> {:white {:rook 1} :black {}}

(fen->captured-pieces "r1bqkb1r/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R")
;=> {:white {} :black {:knight 1}}
```

#### `render-checkerboard-html`

```clojure
(render-checkerboard-html width height top-left-color)
(render-checkerboard-html width height top-left-color dark-color light-color)
```

Generate a complete HTML page with an embedded checkerboard SVG.

**Parameters:**
- `width` - Number of squares wide (integer)
- `height` - Number of squares tall (integer)
- `top-left-color` - `:light` or `:dark`
- `dark-color` - (optional) CSS color value for dark squares (default: `"#769656"`)
- `light-color` - (optional) CSS color value for light squares (default: `"#eeeed2"`)

**Returns:** Complete HTML document string

#### `fen->html-display`

```clojure
(fen->html-display fen)
(fen->html-display fen movetime-ms)
(fen->html-display fen movetime-ms engine-path)
```

Generate a complete HTML page displaying a chess position from FEN with detailed analysis.

**Parameters:**
- `fen` - FEN string (full FEN with game state or just piece placement)
- `movetime-ms` - (optional) Time limit for engine evaluation in milliseconds (default: 1000)
- `engine-path` - (optional) Path to UCI engine executable (default: "stockfish")

**Returns:** Complete HTML document string with responsive layout

Creates an HTML page featuring:
- SVG chessboard showing the position
- Position information panel including:
  - FEN string display
  - Active color (if provided in FEN)
  - Castling rights (if provided)
  - En passant square (if applicable)
  - Halfmove clock (if provided)
  - Fullmove number (if provided)
- Material analysis section:
  - Total piece count
  - White and black material values
  - Material balance with color-coded indicators
  - Engine evaluation (in pawns, from white's perspective) with color-coded indicators
- Captured pieces section showing missing pieces for each side

The page uses a responsive two-column layout (side-by-side on desktop, stacked on mobile) with professional styling.

**Example:**
```clojure
(fen->html-display "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
;=> Returns complete HTML document with default 1000ms evaluation

(fen->html-display "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4" 2000)
;=> Returns HTML with 2000ms evaluation time

(fen->html-display "4k3/8/8/3Q4/8/8/8/4K3" 500 "/usr/local/bin/stockfish")
;=> Returns HTML using custom engine path and 500ms evaluation
```

## Development

### Running Tests

```bash
clojure -M:test
```

### Building

To build a JAR file:

```bash
clojure -T:build jar
```

The JAR will be created in the `target/` directory.

To increment the version:

```bash
clojure -T:build increment-patch
```

To check the current version:

```bash
clojure -T:build get-version
```

### Release Process

Releases are automated via GitHub Actions. When code is pushed to the `main` branch:
1. Tests are run
2. If tests pass, the patch version is automatically incremented
3. A JAR is built
4. A GitHub release is created with:
   - A git tag for easy dependency resolution
   - The JAR as a downloadable artifact (for manual installation)
   - Installation instructions for both git-based and JAR-based approaches


## FEN Notation

FEN (Forsyth-Edwards Notation) is a standard notation for describing chess positions. A FEN string consists of six fields separated by spaces:

1. Piece placement (from white's perspective, rank 8 to rank 1)
2. Active color ("w" or "b")
3. Castling availability (e.g., "KQkq" or "-")
4. En passant target square (e.g., "e3" or "-")
5. Halfmove clock (for the 50-move rule)
6. Fullmove number

Example: `rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1` (starting position)

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
