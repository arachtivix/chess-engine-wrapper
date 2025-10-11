(ns example-display
  "Example usage of chess-engine-wrapper display functionality"
  (:require [chess-engine-wrapper.display :as display]))

(defn -main
  "Generate example HTML files demonstrating display features"
  [& args]
  (println "Chess Engine Wrapper - Display Examples")
  (println "========================================\n")
  
  ;; Example 1: Standard 8x8 chess board with default colors
  (println "Example 1: Generating standard chess board HTML...")
  (spit "example-chess.html"
        (display/render-checkerboard-html 8 8 :dark))
  (println "  Created: example-chess.html")
  
  ;; Example 2: 10x10 checkerboard with light top-left (checkers style)
  (println "\nExample 2: Generating checkers board HTML...")
  (spit "example-checkers.html"
        (display/render-checkerboard-html 10 10 :light))
  (println "  Created: example-checkers.html")
  
  ;; Example 3: Custom size with custom colors
  (println "\nExample 3: Generating custom colored board HTML...")
  (spit "example-custom-colors.html"
        (display/render-checkerboard-html 5 5 :dark "#b58863" "#f0d9b5"))
  (println "  Created: example-custom-colors.html")
  
  ;; Example 4: Board with standard chess position
  (println "\nExample 4: Generating board with standard chess position...")
  (let [svg (display/checkerboard-with-pieces 8 8 :dark (display/standard-chess-position))
        html (str "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                  "<title>Standard Chess Position</title>"
                  "<style>"
                  ".dark-square { fill: #769656; }\n"
                  ".light-square { fill: #eeeed2; }\n"
                  ".chess-piece { font-size: 40px; fill: #000; }"
                  "</style></head><body>"
                  "<div style=\"max-width: 600px; margin: 20px auto;\">"
                  "<h1>Standard Chess Starting Position</h1>"
                  svg
                  "</div></body></html>")]
    (spit "example-standard-position.html" html)
    (println "  Created: example-standard-position.html"))
  
  ;; Example 5: Board with custom piece placement
  (println "\nExample 5: Generating board with custom pieces...")
  (let [pieces {[3 3] :white-queen [3 4] :black-king
                [4 3] :black-knight [4 4] :white-bishop}
        svg (display/checkerboard-with-pieces 8 8 :dark pieces)
        html (str "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                  "<title>Custom Piece Placement</title>"
                  "<style>"
                  ".dark-square { fill: #769656; }\n"
                  ".light-square { fill: #eeeed2; }\n"
                  ".chess-piece { font-size: 40px; fill: #000; }"
                  "</style></head><body>"
                  "<div style=\"max-width: 600px; margin: 20px auto;\">"
                  "<h1>Custom Piece Placement</h1>"
                  "<p>White Queen at d5, Black King at e5, Black Knight at d4, White Bishop at e4</p>"
                  svg
                  "</div></body></html>")]
    (spit "example-custom-pieces.html" html)
    (println "  Created: example-custom-pieces.html"))
  
  ;; Example 6: FEN to pieces conversion
  (println "\nExample 6: Converting FEN to pieces and displaying...")
  (let [fen "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
        pieces (display/fen->pieces fen)
        svg (display/checkerboard-with-pieces 8 8 :dark pieces)
        avg-value (display/fen->avg-material-value fen)
        html (str "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                  "<title>FEN Position Display</title>"
                  "<style>"
                  ".dark-square { fill: #769656; }\n"
                  ".light-square { fill: #eeeed2; }\n"
                  ".chess-piece { font-size: 40px; fill: #000; }"
                  "</style></head><body>"
                  "<div style=\"max-width: 600px; margin: 20px auto;\">"
                  "<h1>Position from FEN</h1>"
                  "<p><strong>FEN:</strong> " fen "</p>"
                  "<p><strong>Piece count:</strong> " (count pieces) " pieces</p>"
                  "<p><strong>Average material value:</strong> " (format "%.2f" avg-value) "</p>"
                  svg
                  "</div></body></html>")]
    (spit "example-fen-position.html" html)
    (println "  Created: example-fen-position.html")
    (println "  Position has " (count pieces) " pieces")
    (println "  Average material value: " (format "%.2f" avg-value)))
  
  (println "\n✓ All examples generated successfully!")
  (println "\nGenerated files:")
  (println "- example-chess.html (8x8 chess board)")
  (println "- example-checkers.html (10x10 checkers board)")
  (println "- example-custom-colors.html (5x5 custom colors)")
  (println "- example-standard-position.html (standard chess starting position)")
  (println "- example-custom-pieces.html (custom piece placement)")
  (println "- example-fen-position.html (position from FEN notation)"))

;; To run this example:
;; clojure -M -m example-display
