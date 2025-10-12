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
      (is (= 0.0 avg))))
  
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

(deftest test-fen->material-balance
  (testing "standard starting position is balanced"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          balance (fen->material-balance fen)]
      (is (= 0 balance))))
  
  (testing "white missing a rook"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1"
          balance (fen->material-balance fen)]
      ; Black is ahead by 5 points (a rook)
      (is (= -5 balance))))
  
  (testing "black missing a rook"
    (let [fen "rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
          balance (fen->material-balance fen)]
      ; White is ahead by 5 points (a rook)
      (is (= 5 balance))))
  
  (testing "white missing a pawn"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPP1/RNBQKBNR"
          balance (fen->material-balance fen)]
      (is (= -1 balance))))
  
  (testing "black missing a queen"
    (let [fen "rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
          balance (fen->material-balance fen)]
      ; White is ahead by 9 points (a queen)
      (is (= 9 balance))))
  
  (testing "complex position"
    (let [fen "rnbqkb1r/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
          balance (fen->material-balance fen)]
      ; White has an extra knight (3 points)
      (is (= 3 balance))))
  
  (testing "empty board is balanced"
    (let [fen "8/8/8/8/8/8/8/8"
          balance (fen->material-balance fen)]
      (is (= 0 balance))))
  
  (testing "only kings is balanced"
    (let [fen "k7/8/8/8/8/8/8/K7"
          balance (fen->material-balance fen)]
      (is (= 0 balance)))))

(deftest test-fen->captured-pieces
  (testing "standard starting position has no captures"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          captured (fen->captured-pieces fen)]
      (is (= {} (:white captured)))
      (is (= {} (:black captured)))))
  
  (testing "white rook captured"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1"
          captured (fen->captured-pieces fen)]
      (is (= {:rook 1} (:white captured)))
      (is (= {} (:black captured)))))
  
  (testing "black rook captured"
    (let [fen "rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
          captured (fen->captured-pieces fen)]
      (is (= {} (:white captured)))
      (is (= {:rook 1} (:black captured)))))
  
  (testing "multiple white pieces captured"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPP2/RNBQKBN1"
          captured (fen->captured-pieces fen)]
      (is (= {:pawn 2 :rook 1} (:white captured)))
      (is (= {} (:black captured)))))
  
  (testing "multiple black pieces captured"
    (let [fen "rnbqkbn1/pppppp2/8/8/8/8/PPPPPPPP/RNBQKBNR"
          captured (fen->captured-pieces fen)]
      (is (= {} (:white captured)))
      (is (= {:pawn 2 :rook 1} (:black captured)))))
  
  (testing "both sides have captures"
    (let [fen "rnbqkbn1/ppppppp1/8/8/8/8/PPPPPPP1/RNBQKBN1"
          captured (fen->captured-pieces fen)]
      (is (= {:pawn 1 :rook 1} (:white captured)))
      (is (= {:pawn 1 :rook 1} (:black captured)))))
  
  (testing "queen captured"
    (let [fen "rnb1kbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
          captured (fen->captured-pieces fen)]
      (is (= {} (:white captured)))
      (is (= {:queen 1} (:black captured)))))
  
  (testing "complex mid-game position"
    (let [fen "r1bqkb1r/pppp1ppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R"
          captured (fen->captured-pieces fen)]
      ; White missing: nothing
      ; Black missing: 1 knight
      (is (= {} (:white captured)))
      (is (= {:knight 1} (:black captured)))))
  
  (testing "empty board captures everything"
    (let [fen "8/8/8/8/8/8/8/8"
          captured (fen->captured-pieces fen)]
      (is (= {:pawn 8 :knight 2 :bishop 2 :rook 2 :queen 1 :king 1} (:white captured)))
      (is (= {:pawn 8 :knight 2 :bishop 2 :rook 2 :queen 1 :king 1} (:black captured))))))

(deftest test-fen->html-display
  (testing "generates valid HTML with standard position"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          html (fen->html-display fen)]
      (is (string? html))
      (is (re-find #"<!DOCTYPE html>" html))
      (is (re-find #"<svg" html))
      (is (re-find #"Chess Position" html))
      (is (re-find #"Active Color" html))
      (is (re-find #"white" html))
      (is (re-find #"Material Balance" html))
      (is (re-find #"Captured Pieces" html))))
  
  (testing "displays material balance correctly"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBN1 w Qkq - 0 1"
          html (fen->html-display fen)]
      (is (re-find #"-5" html))  ; Black ahead by a rook
      (is (re-find #"behind" html))))
  
  (testing "shows captured pieces"
    (let [fen "rnbqkbn1/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQk - 0 1"
          html (fen->html-display fen)]
      (is (re-find #"1 rook" html))))
  
  (testing "handles position with just piece placement"
    (let [fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
          html (fen->html-display fen)]
      (is (string? html))
      (is (re-find #"<svg" html))
      (is (not (re-find #"Active Color" html)))))
  
  (testing "displays en passant and castling"
    (let [fen "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
          html (fen->html-display fen)]
      (is (re-find #"KQkq" html))
      (is (re-find #"e3" html))
      (is (re-find #"black" html))))
  
  (testing "shows halfmove clock and fullmove number"
    (let [fen "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
          html (fen->html-display fen)]
      (is (re-find #"Halfmove Clock" html))
      (is (re-find #"Fullmove Number" html))
      (is (re-find #">4<" html))))
  
  (testing "handles empty board"
    (let [fen "8/8/8/8/8/8/8/8"
          html (fen->html-display fen)]
      (is (string? html))
      (is (re-find #"0" html))))
  
  (testing "calculates material correctly for complex position"
    (let [fen "r1bqkb1r/pppp1ppp/2n2n2/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4"
          html (fen->html-display fen)]
      (is (re-find #"Total Pieces" html))
      (is (re-find #"White Material" html))
      (is (re-find #"Black Material" html)))))
