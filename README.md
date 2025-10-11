# chess-engine-wrapper

A Clojure library that wraps UCI (Universal Chess Interface) standard chess engines to answer simple chess queries. Uses Stockfish by default but supports any UCI-compliant chess engine.

## Features

### Chess Engine Features
- Get all valid FEN positions reachable from a given position in one move
- Evaluate chess positions with configurable time limits
- UCI engine communication abstraction
- Default Stockfish integration with support for other UCI engines

### Display Features
- Generate SVG checkerboards of any width × height dimensions
- Place chess pieces on boards using Unicode symbols
- Convert FEN notation to piece positions
- Calculate average material value from FEN positions
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

## API Reference

### Engine API

#### `get-next-positions`

```clojure
(get-next-positions fen)
(get-next-positions fen engine-path)
```

Given a FEN position string, returns all valid FEN positions reachable by one legal move.

**Parameters:**
- `fen`: A chess position in FEN (Forsyth-Edwards Notation) format
- `engine-path`: (optional) Path to UCI engine executable (defaults to "stockfish")

**Returns:**
A vector of FEN strings representing all positions reachable by one legal move.

**Example:**
```clojure
(get-next-positions "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
;=> ["rnbqkbnr/1ppppppp/p7/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
;    "rnbqkbnr/p1pppppp/1p6/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2"
;    ...]
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
(get-position-value fen movetime-ms engine-path)
```

Get the evaluation of a chess position given in FEN notation.

**Parameters:**
- `fen`: A chess position in FEN notation
- `movetime-ms`: Time limit for computation in milliseconds
- `engine-path`: (optional) Path to UCI engine executable (defaults to "stockfish")

**Returns:**
A map with `:score-cp` (centipawn score from white's perspective) and `:best-move`.
- A positive score favors white, negative favors black
- Score is in centipawns (100 = 1 pawn advantage)
- Returns nil if no evaluation is available

**Example:**
```clojure
(get-position-value "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" 1000)
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
