# chess-engine-wrapper

A Clojure library that wraps UCI (Universal Chess Interface) standard chess engines to answer simple chess queries. Uses Stockfish by default but supports any UCI-compliant chess engine.

## Features

- Get all valid FEN positions reachable from a given position in one move
- Evaluate chess positions with configurable time limits
- UCI engine communication abstraction
- Default Stockfish integration with support for other UCI engines
- Simple, functional API

## Requirements

- Java 8 or higher
- Clojure 1.11.1 or higher
- A UCI-compliant chess engine (Stockfish recommended)

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

Download the latest JAR from the [releases page](https://github.com/arachtivix/chess-engine-wrapper/releases) and add it to your `deps.edn`:

```clojure
{:deps {io.github.arachtivix/chess-engine-wrapper {:local/root "/path/to/chess-engine-wrapper-VERSION.jar"}}}
```

Or use a local checkout:

```clojure
{:deps {chess-engine-wrapper {:local/root "/path/to/chess-engine-wrapper"}}}
```

## Usage

### Basic Usage

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

### `get-next-positions`

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

### `with-engine`

```clojure
(with-engine engine-path f)
```

Execute a function with an initialized engine. The function receives the engine as an argument.

**Parameters:**
- `engine-path`: Path to UCI engine executable
- `f`: Function that takes an engine and performs operations

**Returns:**
The result of calling function `f`

### `get-position-value`

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
4. A GitHub release is created with the JAR as a downloadable artifact

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
