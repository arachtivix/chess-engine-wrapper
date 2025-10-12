(ns example-persistent-session
  "Example demonstrating the use of persistent engine sessions for multiple operations.
  This showcases how to efficiently reuse a single Stockfish instance for multiple
  chess operations, maintaining engine state between requests."
  (:require [chess-engine-wrapper.core :as chess]
            [chess-engine-wrapper.uci :as uci]))

(defn -main
  "Demonstrate persistent engine session usage"
  [& args]
  (println "Chess Engine Wrapper - Persistent Session Example")
  (println "==================================================\n")
  
  ;; Example 1: Using with-engine for multiple operations
  (println "Example 1: Multiple operations with a single engine session")
  (println "------------------------------------------------------------")
  (chess/with-engine "stockfish"
    (fn [engine]
      (let [start-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
            after-e4 "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
            after-e4-e5 "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2"]
        
        ;; First operation - evaluate starting position
        (println "\n1. Evaluating starting position...")
        (let [eval1 (chess/get-position-value start-fen 1000 engine)]
          (println "   Score:" (:score-cp eval1) "cp")
          (println "   Best move:" (:best-move eval1)))
        
        ;; Second operation - get next positions from starting position
        (println "\n2. Getting next positions from start...")
        (let [positions (chess/get-next-positions start-fen engine)]
          (println "   Found" (count positions) "possible next positions"))
        
        ;; Third operation - evaluate position after 1.e4
        (println "\n3. Evaluating position after 1.e4...")
        (let [eval2 (chess/get-position-value after-e4 1000 engine)]
          (println "   Score:" (:score-cp eval2) "cp")
          (println "   Best move:" (:best-move eval2)))
        
        ;; Fourth operation - evaluate position after 1.e4 e5
        (println "\n4. Evaluating position after 1.e4 e5...")
        (let [eval3 (chess/get-position-value after-e4-e5 1000 engine)]
          (println "   Score:" (:score-cp eval3) "cp")
          (println "   Best move:" (:best-move eval3)))
        
        (println "\n   All operations completed using the same engine session!"))))
  
  ;; Example 2: Analyzing a game progression
  (println "\n\nExample 2: Analyzing a game progression")
  (println "----------------------------------------")
  (chess/with-engine "stockfish"
    (fn [engine]
      (let [game-positions [
              "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"      ; Starting
              "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"   ; 1.e4
              "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2" ; 1...e5
              "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2" ; 2.Nf3
              "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3" ; 2...Nc6
              ]]
        (println "\nAnalyzing game progression with a single engine session:\n")
        (doseq [[idx fen] (map-indexed vector game-positions)]
          (let [eval-result (chess/get-position-value fen 500 engine)]
            (println (format "Position %d: Score = %+4d cp, Best move = %s"
                           (inc idx)
                           (:score-cp eval-result)
                           (:best-move eval-result))))))))
  
  ;; Example 3: Direct UCI access with persistent session
  (println "\n\nExample 3: Direct UCI access with persistent session")
  (println "-----------------------------------------------------")
  (chess/with-engine "stockfish"
    (fn [engine]
      (println "\nUsing direct UCI commands with the same engine:")
      
      ;; Set position
      (uci/set-position engine "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
      
      ;; Get legal moves
      (let [moves (uci/get-legal-moves engine)]
        (println "\n1. Legal moves from start:" (count moves) "moves")
        (println "   First 5 moves:" (take 5 moves)))
      
      ;; Evaluate position
      (let [eval-result (uci/get-position-value engine 1000)]
        (println "\n2. Position evaluation:")
        (println "   Score:" (:score-cp eval-result) "cp")
        (println "   Best move:" (:best-move eval-result)))
      
      ;; Change position and evaluate again
      (uci/set-position engine "4k3/8/8/3Q4/8/8/8/4K3 w - - 0 1")
      (let [eval-result (uci/get-position-value engine 500)]
        (println "\n3. Evaluation of endgame position (Queen vs King):")
        (println "   Score:" (:score-cp eval-result) "cp")
        (println "   Best move:" (:best-move eval-result)))))
  
  ;; Comparison with ephemeral sessions
  (println "\n\nComparison: Persistent vs Ephemeral Sessions")
  (println "----------------------------------------------")
  (let [test-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        iterations 5]
    
    ;; Time persistent session
    (print (format "\nTiming %d operations with persistent session... " iterations))
    (flush)
    (let [start-time (System/currentTimeMillis)]
      (chess/with-engine "stockfish"
        (fn [engine]
          (dotimes [_ iterations]
            (chess/get-position-value test-fen 100 engine))))
      (let [persistent-time (- (System/currentTimeMillis) start-time)]
        (println (format "%d ms" persistent-time))
        
        ;; Time ephemeral sessions
        (print (format "Timing %d operations with ephemeral sessions... " iterations))
        (flush)
        (let [start-time (System/currentTimeMillis)]
          (dotimes [_ iterations]
            (chess/get-position-value test-fen 100 "stockfish"))
          (let [ephemeral-time (- (System/currentTimeMillis) start-time)]
            (println (format "%d ms" ephemeral-time))
            (println (format "\nPersistent session saved ~%d ms (%.1fx faster)"
                           (- ephemeral-time persistent-time)
                           (/ (double ephemeral-time) persistent-time)))))))
    
    (println "\n✓ Persistent sessions avoid repeated engine startup/shutdown overhead!"))
  
  (println "\n\n=================================================")
  (println "Examples completed successfully!"))
