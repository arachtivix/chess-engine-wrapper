(ns chess-engine-wrapper.uci
  "UCI (Universal Chess Interface) protocol communication"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn start-engine
  "Start a UCI chess engine process. Returns a map with :process, :in, and :out."
  [engine-path]
  (let [process (-> (ProcessBuilder. [engine-path])
                    (.redirectErrorStream true)
                    (.start))
        stdin (io/writer (.getOutputStream process))
        stdout (io/reader (.getInputStream process))]
    {:process process
     :in stdin
     :out stdout}))

(defn send-command
  "Send a command to the engine"
  [engine command]
  (binding [*out* (:in engine)]
    (println command)
    (flush)))

(defn read-line-with-timeout
  "Read a line from the engine with timeout (in ms). Returns nil on timeout."
  [engine timeout-ms]
  (let [reader (:out engine)
        start (System/currentTimeMillis)]
    (loop []
      (if (.ready reader)
        (.readLine reader)
        (if (> (- (System/currentTimeMillis) start) timeout-ms)
          nil
          (do
            (Thread/sleep 10)
            (recur)))))))

(defn read-until
  "Read lines from engine until predicate is true or timeout (in ms)"
  [engine pred timeout-ms]
  (let [start (System/currentTimeMillis)]
    (loop [lines []]
      (if (> (- (System/currentTimeMillis) start) timeout-ms)
        lines
        (if-let [line (read-line-with-timeout engine (- timeout-ms (- (System/currentTimeMillis) start)))]
          (let [new-lines (conj lines line)]
            (if (pred line)
              new-lines
              (recur new-lines)))
          lines)))))

(defn init-engine
  "Initialize the UCI engine"
  [engine]
  (send-command engine "uci")
  (read-until engine #(= % "uciok") 5000)
  (send-command engine "isready")
  (read-until engine #(= % "readyok") 5000)
  engine)

(defn stop-engine
  "Stop the engine process"
  [engine]
  (try
    (send-command engine "quit")
    (Thread/sleep 100)
    (catch Exception _
      ;; Ignore errors when sending quit command
      nil))
  (try
    (.destroy (:process engine))
    (catch Exception _
      ;; Ignore errors when destroying process
      nil)))

(defn set-position
  "Set position in FEN notation"
  [engine fen]
  (send-command engine (str "position fen " fen)))

(defn get-legal-moves
  "Get all legal moves from current position. Returns a vector of move strings."
  [engine]
  (send-command engine "go perft 1")
  (let [lines (read-until engine #(str/starts-with? % "Nodes searched") 5000)
        move-lines (filter #(and (not (str/blank? %))
                                 (not (str/starts-with? % "Nodes"))
                                 (re-matches #"[a-h][1-8][a-h][1-8].*:.*" %))
                           lines)]
    (mapv #(first (str/split % #":")) move-lines)))

(defn get-position-value
  "Get the evaluation of the current position in centipawns.
  
  Parameters:
  - engine: The engine instance
  - movetime-ms: Time limit for computation in milliseconds
  
  Returns:
  A map with :score-cp (centipawn score from white's perspective) and :best-move.
  Returns nil if no evaluation is available."
  [engine movetime-ms]
  (send-command engine (str "go movetime " movetime-ms))
  (let [lines (read-until engine #(str/starts-with? % "bestmove") (+ movetime-ms 5000))
        ;; Get the last info line with a score
        info-lines (filter #(and (str/starts-with? % "info")
                                (str/includes? % "score cp")) lines)
        last-info (last info-lines)
        bestmove-line (first (filter #(str/starts-with? % "bestmove") lines))]
    (when (and last-info bestmove-line)
      (let [;; Extract score from "info ... score cp 38 ..."
            score-match (re-find #"score cp (-?\d+)" last-info)
            score (when score-match (Integer/parseInt (second score-match)))
            ;; Extract best move from "bestmove e2e4 ..."
            move-match (re-find #"bestmove ([a-h][1-8][a-h][1-8][qrbn]?)" bestmove-line)
            best-move (when move-match (second move-match))]
        {:score-cp score
         :best-move best-move}))))
