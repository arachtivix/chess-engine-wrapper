(ns chess-engine-wrapper.display-test
  (:require [clojure.test :refer :all]
            [chess-engine-wrapper.display :refer :all]))

(deftest test-checkerboard-basic
  (testing "8x8 checkerboard with dark top-left square"
    (let [svg (checkerboard 8 8 :dark)]
      (is (string? svg))
      (is (re-find #"<svg" svg))
      (is (re-find #"viewBox=\"0 0 400 400\"" svg))
      (is (re-find #"dark-square" svg))
      (is (re-find #"light-square" svg)))))

(deftest test-checkerboard-dimensions
  (testing "5x3 checkerboard"
    (let [svg (checkerboard 5 3 :light)]
      (is (re-find #"viewBox=\"0 0 250 150\"" svg))))
  (testing "10x10 checkerboard"
    (let [svg (checkerboard 10 10 :dark)]
      (is (re-find #"viewBox=\"0 0 500 500\"" svg)))))

(deftest test-top-left-color
  (testing "dark top-left square"
    (let [svg (checkerboard 2 2 :dark)]
      ;; Top-left (0,0) should be dark
      (is (re-find #"<rect[^>]*x=\"0\"[^>]*y=\"0\"[^>]*class=\"dark-square\"" svg))))
  (testing "light top-left square"
    (let [svg (checkerboard 2 2 :light)]
      ;; Top-left (0,0) should be light
      (is (re-find #"<rect[^>]*x=\"0\"[^>]*y=\"0\"[^>]*class=\"light-square\"" svg)))))

(deftest test-render-complete-html
  (testing "complete HTML page generation"
    (let [html (render-checkerboard-html 8 8 :dark)]
      (is (re-find #"<html>" html))
      (is (re-find #"<svg" html))
      (is (re-find #"\.dark-square" html))
      (is (re-find #"\.light-square" html)))))

(deftest test-custom-colors
  (testing "custom colors in HTML"
    (let [html (render-checkerboard-html 8 8 :dark "#ff0000" "#00ff00")]
      (is (re-find #"#ff0000" html))
      (is (re-find #"#00ff00" html)))))

(deftest test-checkerboard-with-pieces
  (testing "board with pieces has chess-piece text elements"
    (let [pieces {[0 0] :white-king [7 7] :black-queen}
          svg (checkerboard-with-pieces 8 8 :dark pieces)]
      (is (string? svg))
      (is (re-find #"<svg" svg))
      (is (re-find #"chess-piece" svg))
      (is (re-find #"♔" svg))  ; white king
      (is (re-find #"♛" svg)))) ; black queen
  (testing "empty piece map creates board without pieces"
    (let [svg (checkerboard-with-pieces 8 8 :dark {})]
      (is (re-find #"<svg" svg))
      (is (not (re-find #"chess-piece" svg))))))

(deftest test-standard-chess-position
  (testing "standard chess position has all pieces"
    (let [pos (standard-chess-position)]
      (is (= 32 (count pos)))  ; 32 pieces on the board
      (is (= :white-king (get pos [7 4])))
      (is (= :black-king (get pos [0 4])))
      (is (= :white-rook (get pos [7 0])))
      (is (= :black-rook (get pos [0 0]))))))

(deftest test-fen->pieces
  (testing "standard starting position FEN"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          pieces (fen->pieces fen)]
      (is (= 32 (count pieces)))
      (is (= :black-rook (get pieces [0 0])))
      (is (= :black-king (get pieces [0 4])))
      (is (= :white-king (get pieces [7 4])))
      (is (= :white-rook (get pieces [7 0])))))
  
  (testing "FEN with just piece placement"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
          pieces (fen->pieces fen)]
      (is (= 32 (count pieces)))))
  
  (testing "empty board FEN"
    (let [fen "8/8/8/8/8/8/8/8"
          pieces (fen->pieces fen)]
      (is (= 0 (count pieces)))))
  
  (testing "sparse position FEN"
    (let [fen "4k3/8/8/8/8/8/8/4K3"
          pieces (fen->pieces fen)]
      (is (= 2 (count pieces)))
      (is (= :black-king (get pieces [0 4])))
      (is (= :white-king (get pieces [7 4])))))
  
  (testing "complex mid-game position"
    (let [fen "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R"
          pieces (fen->pieces fen)]
      (is (= :black-rook (get pieces [0 0])))
      (is (= :white-bishop (get pieces [4 2])))  ; Row 4 (rank 4), col 2
      (is (= :black-knight (get pieces [2 2])))  ; Row 2 (rank 6), col 2
      (is (= :white-knight (get pieces [5 5])))))  ; Row 5 (rank 3), col 5
  
  (testing "all piece types"
    (let [fen "KQRBNP2/kqrbnp2/8/8/8/8/8/8"
          pieces (fen->pieces fen)]
      (is (= :white-king (get pieces [0 0])))
      (is (= :white-queen (get pieces [0 1])))
      (is (= :white-rook (get pieces [0 2])))
      (is (= :white-bishop (get pieces [0 3])))
      (is (= :white-knight (get pieces [0 4])))
      (is (= :white-pawn (get pieces [0 5])))
      (is (= :black-king (get pieces [1 0])))
      (is (= :black-queen (get pieces [1 1])))
      (is (= :black-rook (get pieces [1 2])))
      (is (= :black-bishop (get pieces [1 3])))
      (is (= :black-knight (get pieces [1 4])))
      (is (= :black-pawn (get pieces [1 5]))))))

(deftest test-fen->avg-material-value
  (testing "standard starting position average value"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          avg (fen->avg-material-value fen)]
      ; 16 pawns (16*1) + 4 knights (4*3) + 4 bishops (4*3) + 4 rooks (4*5) + 2 queens (2*9) + 2 kings (2*0)
      ; = 16 + 12 + 12 + 20 + 18 + 0 = 78 / 32 = 2.4375
      (is (= 2.4375 avg))))
  
  (testing "empty board average value"
    (let [fen "8/8/8/8/8/8/8/8"
          avg (fen->avg-material-value fen)]
      (is (= 0 avg))))
  
  (testing "only pawns"
    (let [fen "8/pppppppp/8/8/8/8/PPPPPPPP/8"
          avg (fen->avg-material-value fen)]
      (is (= 1.0 avg))))
  
  (testing "only queens"
    (let [fen "q7/8/8/8/8/8/8/Q7"
          avg (fen->avg-material-value fen)]
      (is (= 9.0 avg))))
  
  (testing "kings only have zero value"
    (let [fen "k7/8/8/8/8/8/8/K7"
          avg (fen->avg-material-value fen)]
      (is (= 0.0 avg))))
  
  (testing "mixed pieces"
    (let [fen "rnbq4/8/8/8/8/8/8/RNBQ4"
          avg (fen->avg-material-value fen)]
      ; 2 rooks (2*5) + 2 knights (2*3) + 2 bishops (2*3) + 2 queens (2*9)
      ; = 10 + 6 + 6 + 18 = 40 / 8 = 5.0
      (is (= 5.0 avg)))))
