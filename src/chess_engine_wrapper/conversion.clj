(ns chess-engine-wrapper.conversion
  "Conversion utilities for FEN notation and chess piece formats")

(defn fen-char->piece
  "Convert a FEN character to a piece keyword.
  
  Parameters:
  - c: FEN character (uppercase for white, lowercase for black)
  
  Returns piece keyword or nil if not a piece."
  [c]
  (case c
    \P :white-pawn
    \N :white-knight
    \B :white-bishop
    \R :white-rook
    \Q :white-queen
    \K :white-king
    \p :black-pawn
    \n :black-knight
    \b :black-bishop
    \r :black-rook
    \q :black-queen
    \k :black-king
    nil))

(defn piece-unicode
  "Get Unicode character for a chess piece.
  
  Parameters:
  - piece: keyword like :white-king, :black-pawn, etc.
  
  Returns Unicode character for the piece."
  [piece]
  (case piece
    :white-king "♔"
    :white-queen "♕"
    :white-rook "♖"
    :white-bishop "♗"
    :white-knight "♘"
    :white-pawn "♙"
    :black-king "♚"
    :black-queen "♛"
    :black-rook "♜"
    :black-bishop "♝"
    :black-knight "♞"
    :black-pawn "♟"
    ""))

(defn piece->material-value
  "Get the material value of a chess piece.
  
  Standard values: Pawn=1, Knight=3, Bishop=3, Rook=5, Queen=9, King=0"
  [piece]
  (case piece
    (:white-pawn :black-pawn) 1
    (:white-knight :black-knight) 3
    (:white-bishop :black-bishop) 3
    (:white-rook :black-rook) 5
    (:white-queen :black-queen) 9
    (:white-king :black-king) 0
    0))

(defn fen->pieces
  "Convert FEN notation to pieces map format.
  
  FEN notation describes piece positions starting from rank 8 (row 0) down to rank 1 (row 7).
  Each rank is separated by '/', and numbers indicate empty squares.
  
  Parameters:
  - fen: FEN string (can be full FEN or just the piece placement part)
  
  Returns a map of [row col] -> piece keyword.
  
  Example:
  (fen->pieces \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR\")
  ;; Returns standard chess starting position"
  [fen]
  (let [piece-placement (first (clojure.string/split fen #"\s+"))
        ranks (clojure.string/split piece-placement #"/")]
    (into {}
          (for [[row-idx rank] (map-indexed vector ranks)
                [col-idx piece] (->> rank
                                     (mapcat (fn [c]
                                               (if (Character/isDigit c)
                                                 (repeat (Character/digit c 10) nil)
                                                 [c])))
                                     (map-indexed vector))
                :when piece]
            [[row-idx col-idx] (fen-char->piece piece)]))))
