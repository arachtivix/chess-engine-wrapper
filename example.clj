(ns example
  "Example usage of chess-engine-wrapper library"
  (:require [chess-engine-wrapper.core :as chess]))

(defn -main []
  (println "Chess Engine Wrapper - Example Usage")
  (println "=====================================\n")
  
  ;; Example 1: Get all positions from starting position
  (println "Example 1: Starting Position")
  (let [start-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        positions (chess/get-next-positions start-fen)]
    (println (str "  Found " (count positions) " legal opening moves"))
    (println "  First 3 positions:")
    (doseq [pos (take 3 positions)]
      (println (str "    " pos))))
  
  (println "\nExample 2: After 1.e4")
  (let [after-e4 "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
        positions (chess/get-next-positions after-e4)]
    (println (str "  Found " (count positions) " legal moves for Black"))
    (println "  Sample position after e4-e5:")
    (println (str "    " (first (filter #(re-find #"e5" %) positions)))))
  
  (println "\n✓ Examples completed successfully!"))

;; To run this example:
;; clojure -M -m example
