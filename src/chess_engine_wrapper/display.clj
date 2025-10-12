(ns chess-engine-wrapper.display
  "Chess board display functionality - generate SVG checkerboards with pieces"
  (:require [chess-engine-wrapper.core]))

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
      0.0
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

(defn- parse-fen-info
  "Parse full FEN string to extract all fields.
  
  Parameters:
  - fen: Full FEN string
  
  Returns a map with keys:
  - :piece-placement - the piece placement part
  - :active-color - :white or :black
  - :castling - string of castling availability (e.g., \"KQkq\", \"-\")
  - :en-passant - en passant target square or \"-\"
  - :halfmove-clock - number of halfmoves since last capture or pawn move
  - :fullmove-number - current move number"
  [fen]
  (let [parts (clojure.string/split fen #"\s+")
        [piece-placement active-color castling en-passant halfmove fullmove] parts]
    {:piece-placement piece-placement
     :active-color (case active-color
                     "w" :white
                     "b" :black
                     nil)
     :castling (or castling "-")
     :en-passant (or en-passant "-")
     :halfmove-clock (if halfmove (Integer/parseInt halfmove) nil)
     :fullmove-number (if fullmove (Integer/parseInt fullmove) nil)}))

(defn fen->html-display
  "Generate a complete HTML page displaying a chess position from FEN with detailed information.
  
  Creates an HTML page with:
  - SVG board showing the position
  - Side panel with position information (FEN, piece count, material balance, captured pieces, engine evaluation, etc.)
  
  Parameters:
  - fen: FEN string (full FEN with game state or just piece placement)
  - movetime-ms: (optional) Time limit for engine evaluation in milliseconds (default: 1000)
  - engine-path: (optional) Path to UCI engine executable (default: \"stockfish\")
  
  Returns a complete HTML document string.
  
  Example:
  (fen->html-display \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\")
  (fen->html-display \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\" 2000)
  (fen->html-display \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\" 2000 \"/path/to/engine\")"
  ([fen]
   (fen->html-display fen 1000))
  ([fen movetime-ms]
   (fen->html-display fen movetime-ms "stockfish"))
  ([fen movetime-ms engine-path]
   (let [pieces (fen->pieces fen)
         svg (checkerboard-with-pieces 8 8 :dark pieces)
         piece-count (count pieces)
         balance (fen->material-balance fen)
         captured (fen->captured-pieces fen)
         fen-info (parse-fen-info fen)
         
         ;; Get engine evaluation
         engine-eval (try
                       (chess-engine-wrapper.core/get-position-value fen movetime-ms engine-path)
                       (catch Exception e nil))
        
        ;; Helper to format captured pieces
        format-captured (fn [piece-map]
                          (if (empty? piece-map)
                            "None"
                            (clojure.string/join ", " 
                              (map (fn [[piece count]]
                                     (str count " " (name piece) (if (> count 1) "s" "")))
                                   piece-map))))
        
        ;; Calculate material values
        white-material (reduce + (map (comp piece->material-value second)
                                      (filter (fn [[_ piece]]
                                                (#{:white-pawn :white-knight :white-bishop
                                                   :white-rook :white-queen :white-king} piece))
                                              pieces)))
        black-material (reduce + (map (comp piece->material-value second)
                                      (filter (fn [[_ piece]]
                                                (#{:black-pawn :black-knight :black-bishop
                                                   :black-rook :black-queen :black-king} piece))
                                              pieces)))]
    (str "<!DOCTYPE html>"
         "<html>"
         "<head>"
         "<meta charset=\"UTF-8\">"
         "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
         "<title>Chess Position from FEN</title>"
         "<style>"
         ".dark-square { fill: #769656; }\n"
         ".light-square { fill: #eeeed2; }\n"
         ".chess-piece { font-size: 40px; fill: #000; }\n"
         "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }\n"
         ".container { max-width: 1200px; margin: 0 auto; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden; }\n"
         ".content { display: flex; flex-wrap: wrap; }\n"
         ".board-section { flex: 1; min-width: 300px; padding: 30px; }\n"
         ".info-section { flex: 1; min-width: 300px; padding: 30px; background: #fafafa; border-left: 1px solid #e0e0e0; }\n"
         "h1 { margin: 0 0 20px 0; color: #333; font-size: 24px; }\n"
         "h2 { margin: 20px 0 10px 0; color: #555; font-size: 18px; border-bottom: 2px solid #769656; padding-bottom: 5px; }\n"
         ".info-row { margin: 8px 0; display: flex; justify-content: space-between; align-items: baseline; }\n"
         ".info-label { font-weight: 600; color: #666; }\n"
         ".info-value { color: #333; font-family: 'Courier New', monospace; }\n"
         ".fen-display { background: #f0f0f0; padding: 10px; border-radius: 4px; word-break: break-all; font-family: 'Courier New', monospace; font-size: 12px; margin: 10px 0; }\n"
         ".positive { color: #2e7d32; }\n"
         ".negative { color: #c62828; }\n"
         ".neutral { color: #666; }\n"
         "@media (max-width: 768px) { .content { flex-direction: column; } .info-section { border-left: none; border-top: 1px solid #e0e0e0; } }\n"
         "</style>"
         "</head>"
         "<body>"
         "<div class=\"container\">"
         "<div class=\"content\">"
         
         ;; Board section
         "<div class=\"board-section\">"
         "<h1>Chess Position</h1>"
         svg
         "</div>"
         
         ;; Info section
         "<div class=\"info-section\">"
         "<h2>Position Information</h2>"
         "<div class=\"fen-display\">" (html-escape fen) "</div>"
         
         (when (:active-color fen-info)
           (str "<div class=\"info-row\">"
                "<span class=\"info-label\">Active Color:</span>"
                "<span class=\"info-value\">" (name (:active-color fen-info)) "</span>"
                "</div>"))
         
         (when (and (:castling fen-info) (not= "-" (:castling fen-info)))
           (str "<div class=\"info-row\">"
                "<span class=\"info-label\">Castling:</span>"
                "<span class=\"info-value\">" (html-escape (:castling fen-info)) "</span>"
                "</div>"))
         
         (when (and (:en-passant fen-info) (not= "-" (:en-passant fen-info)))
           (str "<div class=\"info-row\">"
                "<span class=\"info-label\">En Passant:</span>"
                "<span class=\"info-value\">" (html-escape (:en-passant fen-info)) "</span>"
                "</div>"))
         
         (when (:halfmove-clock fen-info)
           (str "<div class=\"info-row\">"
                "<span class=\"info-label\">Halfmove Clock:</span>"
                "<span class=\"info-value\">" (:halfmove-clock fen-info) "</span>"
                "</div>"))
         
         (when (:fullmove-number fen-info)
           (str "<div class=\"info-row\">"
                "<span class=\"info-label\">Fullmove Number:</span>"
                "<span class=\"info-value\">" (:fullmove-number fen-info) "</span>"
                "</div>"))
         
         "<h2>Material Analysis</h2>"
         "<div class=\"info-row\">"
         "<span class=\"info-label\">Total Pieces:</span>"
         "<span class=\"info-value\">" piece-count "</span>"
         "</div>"
         
         "<div class=\"info-row\">"
         "<span class=\"info-label\">White Material:</span>"
         "<span class=\"info-value\">" white-material " points</span>"
         "</div>"
         
         "<div class=\"info-row\">"
         "<span class=\"info-label\">Black Material:</span>"
         "<span class=\"info-value\">" black-material " points</span>"
         "</div>"
         
         "<div class=\"info-row\">"
         "<span class=\"info-label\">Material Balance:</span>"
         "<span class=\"info-value " 
         (cond
           (pos? balance) "positive"
           (neg? balance) "negative"
           :else "neutral")
         "\">"
         (if (pos? balance) "+" "") balance " (White " 
         (cond
           (pos? balance) "ahead"
           (neg? balance) "behind"
           :else "even")
         ")</span>"
         "</div>"
         
         (when engine-eval
           (str "<div class=\"info-row\">"
                "<span class=\"info-label\">Engine Evaluation:</span>"
                "<span class=\"info-value " 
                (cond
                  (pos? (:score-cp engine-eval)) "positive"
                  (neg? (:score-cp engine-eval)) "negative"
                  :else "neutral")
                "\">"
                (if (pos? (:score-cp engine-eval)) "+" "") 
                (format "%.2f" (/ (:score-cp engine-eval) 100.0))
                " pawns"
                "</span>"
                "</div>"))
         
         "<h2>Captured Pieces</h2>"
         "<div class=\"info-row\">"
         "<span class=\"info-label\">White Captured:</span>"
         "<span class=\"info-value\">" (format-captured (:white captured)) "</span>"
         "</div>"
         
         "<div class=\"info-row\">"
         "<span class=\"info-label\">Black Captured:</span>"
         "<span class=\"info-value\">" (format-captured (:black captured)) "</span>"
         "</div>"
         
         "</div>" ; info-section
         "</div>" ; content
         "</div>" ; container
         "</body>"
         "</html>"))))
