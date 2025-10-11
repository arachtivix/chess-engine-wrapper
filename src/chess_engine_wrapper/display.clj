(ns chess-engine-wrapper.display
  "Chess board display functionality - generate SVG checkerboards with pieces")

(defn- html-escape
  "Escape HTML special characters."
  [s]
  (-> s
      (clojure.string/replace "&" "&amp;")
      (clojure.string/replace "<" "&lt;")
      (clojure.string/replace ">" "&gt;")
      (clojure.string/replace "\"" "&quot;")))

(defn checkerboard
  "Generate an HTML SVG checkerboard.
  
  Parameters:
  - width: number of squares wide
  - height: number of squares tall
  - top-left-color: :light or :dark, determines the color of the top-left square
  - square-size: (optional) size of each square in pixels (default: 50)
  
  Returns an SVG string with CSS-responsive dark squares."
  ([width height top-left-color]
   (checkerboard width height top-left-color 50))
  ([width height top-left-color square-size]
   (let [svg-width (* width square-size)
         svg-height (* height square-size)
         squares (for [row (range height)
                       col (range width)]
                   (let [is-even-sum (even? (+ row col))
                         is-dark (if (= top-left-color :dark)
                                  is-even-sum
                                  (not is-even-sum))
                         x (* col square-size)
                         y (* row square-size)
                         css-class (if is-dark "dark-square" "light-square")]
                     (format "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" class=\"%s\"/>"
                             x y square-size square-size css-class)))]
     (str "<svg width=\"100%\" height=\"100%\" viewBox=\"0 0 " svg-width " " svg-height "\" xmlns=\"http://www.w3.org/2000/svg\">"
          (clojure.string/join "" squares)
          "</svg>"))))

(defn- piece-unicode
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

(defn standard-chess-position
  "Returns a map of the standard chess starting position.
  
  Uses standard chess notation where row 0 is the top (rank 8) and row 7 is bottom (rank 1).
  Returns a map of [row col] -> piece keyword."
  []
  {;; Black pieces (top of board)
   [0 0] :black-rook    [0 1] :black-knight  [0 2] :black-bishop  [0 3] :black-queen
   [0 4] :black-king    [0 5] :black-bishop  [0 6] :black-knight  [0 7] :black-rook
   [1 0] :black-pawn    [1 1] :black-pawn    [1 2] :black-pawn    [1 3] :black-pawn
   [1 4] :black-pawn    [1 5] :black-pawn    [1 6] :black-pawn    [1 7] :black-pawn
   ;; White pieces (bottom of board)
   [6 0] :white-pawn    [6 1] :white-pawn    [6 2] :white-pawn    [6 3] :white-pawn
   [6 4] :white-pawn    [6 5] :white-pawn    [6 6] :white-pawn    [6 7] :white-pawn
   [7 0] :white-rook    [7 1] :white-knight  [7 2] :white-bishop  [7 3] :white-queen
   [7 4] :white-king    [7 5] :white-bishop  [7 6] :white-knight  [7 7] :white-rook})

(defn checkerboard-with-pieces
  "Generate an HTML SVG checkerboard with chess pieces.
  
  Parameters:
  - width: number of squares wide
  - height: number of squares tall
  - top-left-color: :light or :dark, determines the color of the top-left square
  - pieces: map of positions to piece keywords, e.g. {[0 0] :white-rook, [0 1] :white-knight}
            positions are [row col] with [0 0] being top-left
  - square-size: (optional) size of each square in pixels (default: 50)
  
  Returns an SVG string with CSS-responsive dark squares and pieces."
  ([width height top-left-color pieces]
   (checkerboard-with-pieces width height top-left-color pieces 50))
  ([width height top-left-color pieces square-size]
   (let [svg-width (* width square-size)
         svg-height (* height square-size)
         squares (for [row (range height)
                       col (range width)]
                   (let [is-even-sum (even? (+ row col))
                         is-dark (if (= top-left-color :dark)
                                  is-even-sum
                                  (not is-even-sum))
                         x (* col square-size)
                         y (* row square-size)
                         css-class (if is-dark "dark-square" "light-square")]
                     (format "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" class=\"%s\"/>"
                             x y square-size square-size css-class)))
         piece-elements (for [[[row col] piece] pieces]
                         (let [x (* col square-size)
                               y (* row square-size)
                               text-x (+ x (/ square-size 2))
                               text-y (+ y (/ square-size 2))
                               unicode-char (piece-unicode piece)]
                           (format "<text x=\"%d\" y=\"%d\" class=\"chess-piece\" text-anchor=\"middle\" dominant-baseline=\"central\">%s</text>"
                                   text-x text-y unicode-char)))]
     (str "<svg width=\"100%\" height=\"100%\" viewBox=\"0 0 " svg-width " " svg-height "\" xmlns=\"http://www.w3.org/2000/svg\">"
          (clojure.string/join "" squares)
          (clojure.string/join "" piece-elements)
          "</svg>"))))

(defn render-checkerboard-html
  "Generate a complete HTML page with an embedded checkerboard SVG.
  
  Parameters:
  - width: number of squares wide
  - height: number of squares tall
  - top-left-color: :light or :dark
  - dark-color: CSS color value for dark squares (default: \"#769656\")
  - light-color: CSS color value for light squares (default: \"#eeeed2\")
  
  Returns a complete HTML document string."
  ([width height top-left-color]
   (render-checkerboard-html width height top-left-color "#769656" "#eeeed2"))
  ([width height top-left-color dark-color light-color]
   (str "<!DOCTYPE html>"
        "<html>"
        "<head>"
        "<meta charset=\"UTF-8\">"
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
        "<title>" width "x" height " Checkerboard</title>"
        "<style>"
        ".dark-square { fill: " dark-color "; }\n"
        ".light-square { fill: " light-color "; }"
        "</style>"
        "</head>"
        "<body>"
        "<div style=\"max-width: 600px; margin: 0 auto;\">"
        (checkerboard width height top-left-color)
        "</div>"
        "</body>"
        "</html>")))

(defn- fen-char->piece
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

(defn- piece->material-value
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

(defn fen->avg-material-value
  "Calculate the average material value from a FEN position.
  
  Returns the total material value divided by the number of pieces on the board.
  Kings are counted but have a value of 0.
  
  Parameters:
  - fen: FEN string (can be full FEN or just the piece placement part)
  
  Returns average material value as a number.
  
  Example:
  (fen->avg-material-value \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR\")
  ;; Returns 2.4375 (total value 78 / 32 pieces)"
  [fen]
  (let [pieces (fen->pieces fen)
        piece-count (count pieces)]
    (if (zero? piece-count)
      0
      (double (/ (reduce + (map (comp piece->material-value second) pieces))
                 piece-count)))))

(defn fen->material-balance
  "Calculate the material balance from a FEN position in pawns worth.
  
  Returns the difference between white's material and black's material.
  Positive values indicate white is ahead, negative values indicate black is ahead.
  Kings have a value of 0 and do not affect the balance.
  
  Parameters:
  - fen: FEN string (can be full FEN or just the piece placement part)
  
  Returns material balance as an integer (in pawn units).
  
  Example:
  (fen->material-balance \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR\")
  ;; Returns 0 (balanced position)
  
  (fen->material-balance \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1\")
  ;; Returns -5 (black is ahead by a rook)"
  [fen]
  (let [pieces (fen->pieces fen)
        white-pieces (filter (fn [[_ piece]] 
                               (#{:white-pawn :white-knight :white-bishop 
                                  :white-rook :white-queen :white-king} piece)) 
                             pieces)
        black-pieces (filter (fn [[_ piece]] 
                               (#{:black-pawn :black-knight :black-bishop 
                                  :black-rook :black-queen :black-king} piece)) 
                             pieces)
        white-value (reduce + (map (comp piece->material-value second) white-pieces))
        black-value (reduce + (map (comp piece->material-value second) black-pieces))]
    (- white-value black-value)))

(defn- standard-starting-pieces
  "Returns the pieces that should be in a standard chess starting position."
  []
  {:white {:pawn 8 :knight 2 :bishop 2 :rook 2 :queen 1 :king 1}
   :black {:pawn 8 :knight 2 :bishop 2 :rook 2 :queen 1 :king 1}})

(defn fen->captured-pieces
  "Determine which pieces have been captured based on a FEN position.
  
  Compares the current position to the standard starting position to identify
  missing pieces for each side.
  
  Parameters:
  - fen: FEN string (can be full FEN or just the piece placement part)
  
  Returns a map with :white and :black keys, each containing a map of piece types
  to counts of captured pieces.
  
  Example:
  (fen->captured-pieces \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1\")
  ;; Returns {:white {:rook 1} :black {}}
  
  (fen->captured-pieces \"rnbqkbnr/ppp1pppp/8/8/8/8/PPPPPPPP/RNBQKBNR\")
  ;; Returns {:white {} :black {:pawn 1}}"
  [fen]
  (let [pieces (fen->pieces fen)
        current-white (frequencies 
                       (map (fn [[_ piece]]
                              (case piece
                                :white-pawn :pawn
                                :white-knight :knight
                                :white-bishop :bishop
                                :white-rook :rook
                                :white-queen :queen
                                :white-king :king
                                nil))
                            (filter (fn [[_ piece]] 
                                      (#{:white-pawn :white-knight :white-bishop 
                                         :white-rook :white-queen :white-king} piece)) 
                                    pieces)))
        current-black (frequencies 
                       (map (fn [[_ piece]]
                              (case piece
                                :black-pawn :pawn
                                :black-knight :knight
                                :black-bishop :bishop
                                :black-rook :rook
                                :black-queen :queen
                                :black-king :king
                                nil))
                            (filter (fn [[_ piece]] 
                                      (#{:black-pawn :black-knight :black-bishop 
                                         :black-rook :black-queen :black-king} piece)) 
                                    pieces)))
        starting (standard-starting-pieces)
        white-captured (into {} 
                             (for [[piece-type start-count] (:white starting)
                                   :let [current-count (get current-white piece-type 0)
                                         captured (- start-count current-count)]
                                   :when (pos? captured)]
                               [piece-type captured]))
        black-captured (into {} 
                             (for [[piece-type start-count] (:black starting)
                                   :let [current-count (get current-black piece-type 0)
                                         captured (- start-count current-count)]
                                   :when (pos? captured)]
                               [piece-type captured]))]
    {:white white-captured
     :black black-captured}))
