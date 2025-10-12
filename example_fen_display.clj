(ns example-fen-display
  "Example usage of fen->html-display function"
  (:require [chess-engine-wrapper.display :as display]))

(defn -main
  "Generate example HTML files demonstrating fen->html-display feature"
  [& args]
  (println "Chess Engine Wrapper - FEN HTML Display Examples")
  (println "=================================================\n")
  
  ;; Example 1: Standard starting position
  (println "Example 1: Standard starting position...")
  (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        html (display/fen->html-display fen)]
    (spit "fen-display-starting-position.html" html)
    (println "  Created: fen-display-starting-position.html"))
  
  ;; Example 2: Famous position - Italian Game
  (println "\nExample 2: Italian Game opening...")
  (let [fen "r1bqkbnr/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 4 4"
        html (display/fen->html-display fen)]
    (spit "fen-display-italian-game.html" html)
    (println "  Created: fen-display-italian-game.html"))
  
  ;; Example 3: Material imbalance - white is ahead
  (println "\nExample 3: Position with material imbalance (white ahead)...")
  (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1 w Qkq - 0 1"
        html (display/fen->html-display fen)]
    (spit "fen-display-white-ahead.html" html)
    (println "  Created: fen-display-white-ahead.html"))
  
  ;; Example 4: Material imbalance - black is ahead
  (println "\nExample 4: Position with material imbalance (black ahead)...")
  (let [fen "rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQk - 0 1"
        html (display/fen->html-display fen)]
    (spit "fen-display-black-ahead.html" html)
    (println "  Created: fen-display-black-ahead.html"))
  
  ;; Example 5: Endgame position
  (println "\nExample 5: Endgame position...")
  (let [fen "8/5k2/3p4/1p1Pp3/pP2P1K1/P7/8/8 w - - 99 50"
        html (display/fen->html-display fen)]
    (spit "fen-display-endgame.html" html)
    (println "  Created: fen-display-endgame.html"))
  
  ;; Example 6: Position with en passant
  (println "\nExample 6: Position with en passant opportunity...")
  (let [fen "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3"
        html (display/fen->html-display fen)]
    (spit "fen-display-en-passant.html" html)
    (println "  Created: fen-display-en-passant.html"))
  
  ;; Example 7: Position with limited castling rights
  (println "\nExample 7: Position with limited castling rights...")
  (let [fen "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/3P1N2/PPP2PPP/RNBQK2R w KQkq - 6 5"
        html (display/fen->html-display fen)]
    (spit "fen-display-castling.html" html)
    (println "  Created: fen-display-castling.html"))
  
  ;; Example 8: Position with only piece placement (no game state)
  (println "\nExample 8: Position with only piece placement...")
  (let [fen "4k3/8/8/3Q4/8/8/8/4K3"
        html (display/fen->html-display fen)]
    (spit "fen-display-simple-endgame.html" html)
    (println "  Created: fen-display-simple-endgame.html"))
  
  ;; Example 9: Complex middlegame position
  (println "\nExample 9: Complex middlegame position...")
  (let [fen "r2qk2r/ppp2ppp/2np1n2/2b1p1B1/2B1P1b1/2NP1N2/PPP2PPP/R2QK2R w KQkq - 8 8"
        html (display/fen->html-display fen)]
    (spit "fen-display-middlegame.html" html)
    (println "  Created: fen-display-middlegame.html"))
  
  ;; Example 10: Position with many captures
  (println "\nExample 10: Position with many captures...")
  (let [fen "2k5/8/8/8/8/8/8/2K5 w - - 0 40"
        html (display/fen->html-display fen)]
    (spit "fen-display-many-captures.html" html)
    (println "  Created: fen-display-many-captures.html"))
  
  (println "\n✓ All examples generated successfully!")
  (println "\nGenerated files:")
  (println "- fen-display-starting-position.html (standard starting position)")
  (println "- fen-display-italian-game.html (Italian Game opening)")
  (println "- fen-display-white-ahead.html (white ahead in material)")
  (println "- fen-display-black-ahead.html (black ahead in material)")
  (println "- fen-display-endgame.html (pawn endgame)")
  (println "- fen-display-en-passant.html (with en passant)")
  (println "- fen-display-castling.html (with castling rights)")
  (println "- fen-display-simple-endgame.html (simple position)")
  (println "- fen-display-middlegame.html (complex middlegame)")
  (println "- fen-display-many-captures.html (position with many captures)"))

;; To run this example:
;; clojure -M -m example-fen-display
