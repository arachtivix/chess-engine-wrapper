(ns chess-engine-wrapper.core
  "Main API for chess engine wrapper"
  (:require [chess-engine-wrapper.uci :as uci]
            [clojure.string :as str]))

(def ^:dynamic *default-engine-path* "stockfish")

(defn- parse-fen
  "Parse FEN string into components"
  [fen]
  (let [parts (str/split fen #" ")]
    {:position (nth parts 0 "")
     :active-color (nth parts 1 "w")
     :castling (nth parts 2 "-")
     :en-passant (nth parts 3 "-")
     :halfmove (nth parts 4 "0")
     :fullmove (nth parts 5 "1")}))

(defn- rebuild-fen
  "Rebuild FEN string from components"
  [{:keys [position active-color castling en-passant halfmove fullmove]}]
  (str/join " " [position active-color castling en-passant halfmove fullmove]))

(defn- apply-move-to-fen
  "Apply a UCI move to a FEN position. This is a simplified version that
  relies on the engine to generate the resulting position."
  [engine fen move]
  ;; Set position and make the move
  (uci/send-command engine (str "position fen " fen " moves " move))
  (uci/send-command engine "d")
  
  ;; Read the output and extract FEN
  (let [lines (uci/read-until engine #(str/starts-with? % "Checkers") 2000)
        fen-line (first (filter #(str/starts-with? % "Fen:") lines))]
    (when fen-line
      (str/trim (subs fen-line 4)))))

(defn get-next-positions
  "Given a FEN position, return all valid FEN positions reachable by one move.
  
  Parameters:
  - fen: A position in FEN notation
  - engine-or-path: Either an initialized engine instance (map with :process, :in, :out) 
                    or a path to UCI engine executable (string, defaults to 'stockfish')
  
  Returns:
  A vector of FEN strings representing all positions reachable by one legal move.
  
  When passed an engine instance, the engine session is maintained and can be reused.
  When passed a string path, creates an ephemeral engine session."
  ([fen]
   (get-next-positions fen *default-engine-path*))
  ([fen engine-or-path]
   (if (map? engine-or-path)
     ;; engine-or-path is an engine instance - use it directly without stopping
     (do
       (uci/set-position engine-or-path fen)
       (let [moves (uci/get-legal-moves engine-or-path)]
         (vec (keep (fn [move]
                      (apply-move-to-fen engine-or-path fen move))
                    moves))))
     ;; engine-or-path is a string path - create ephemeral session
     (let [engine (-> (uci/start-engine engine-or-path)
                      (uci/init-engine))]
       (try
         (uci/set-position engine fen)
         (let [moves (uci/get-legal-moves engine)]
           (vec (keep (fn [move]
                        (apply-move-to-fen engine fen move))
                      moves)))
         (finally
           (uci/stop-engine engine)))))))

(defn get-position-value
  "Get the evaluation of a chess position given in FEN notation.
  
  Parameters:
  - fen: A chess position in FEN notation
  - movetime-ms: Time limit for computation in milliseconds
  - engine-or-path: (optional) Either an initialized engine instance (map with :process, :in, :out)
                    or a path to UCI engine executable (string, defaults to 'stockfish')
  
  Returns:
  A map with :score-cp (centipawn score from white's perspective) and :best-move.
  A positive score favors white, negative favors black.
  Returns nil if no evaluation is available.
  
  When passed an engine instance, the engine session is maintained and can be reused.
  When passed a string path, creates an ephemeral engine session."
  ([fen movetime-ms]
   (get-position-value fen movetime-ms *default-engine-path*))
  ([fen movetime-ms engine-or-path]
   (if (map? engine-or-path)
     ;; engine-or-path is an engine instance - use it directly without stopping
     (do
       (uci/set-position engine-or-path fen)
       (uci/get-position-value engine-or-path movetime-ms))
     ;; engine-or-path is a string path - create ephemeral session
     (let [engine (-> (uci/start-engine engine-or-path)
                      (uci/init-engine))]
       (try
         (uci/set-position engine fen)
         (uci/get-position-value engine movetime-ms)
         (finally
           (uci/stop-engine engine)))))))

(defn with-engine
  "Execute a function with an initialized engine. The function receives the engine as argument.
  
  Example:
  (with-engine \"stockfish\"
    (fn [engine]
      (uci/set-position engine \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\")
      (uci/get-legal-moves engine)))"
  [engine-path f]
  (let [engine (-> (uci/start-engine engine-path)
                   (uci/init-engine))]
    (try
      (f engine)
      (finally
        (uci/stop-engine engine)))))
