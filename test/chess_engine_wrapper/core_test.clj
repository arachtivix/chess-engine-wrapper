(ns chess-engine-wrapper.core-test
  (:require [clojure.test :refer :all]
            [chess-engine-wrapper.core :as core]
            [chess-engine-wrapper.uci :as uci]))

(deftest test-get-next-positions-startpos
  (testing "Getting next positions from starting position"
    (let [start-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          result (core/get-next-positions start-fen)]
      (is (= 20 (count result)) "Starting position should have 20 legal moves")
      (is (every? string? result) "All results should be strings")
      (is (every? #(re-matches #"[rnbqkpRNBQKP0-9/]+ [wb] [KQkq-]+ [a-h0-9-]+ \d+ \d+" %) result)
          "All results should be valid FEN strings"))))

(deftest test-get-next-positions-after-e4
  (testing "Getting next positions after 1.e4"
    (let [after-e4 "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
          result (core/get-next-positions after-e4)]
      (is (= 20 (count result)) "Black should have 20 legal moves after 1.e4")
      (is (every? string? result) "All results should be strings"))))

(deftest test-get-next-positions-limited-moves
  (testing "Getting next positions from a position with limited moves"
    ;; Position where white king has only a few moves (both kings on the board)
    (let [limited-fen "4k3/8/8/8/8/8/8/4K3 w - - 0 1"
          result (core/get-next-positions limited-fen)]
      (is (= 5 (count result)) "King in center should have 5 legal moves")
      (is (every? string? result) "All results should be strings"))))

(deftest test-with-engine
  (testing "with-engine helper function"
    (let [result (core/with-engine "stockfish"
                   (fn [engine]
                     (uci/set-position engine "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                     (uci/get-legal-moves engine)))]
      (is (= 20 (count result)) "Should return 20 legal moves from start position")
      (is (every? string? result) "All moves should be strings"))))

(deftest test-get-next-positions-18-moves
  (testing "Getting next positions from a position with 18 legal moves"
    ;; Position with pawns on a3 and b3, blocking double moves from a2 and b2
    ;; This gives: a3->a4 (1) + b3->b4 (1) + c2-h2 single/double moves (12) + 2 knights (4) = 18 moves
    (let [fen-18 "rnbqkbnr/pppppppp/8/8/8/PP6/2PPPPPP/RNBQKBNR w KQkq - 0 1"
          result (core/get-next-positions fen-18)]
      (is (= 18 (count result)) "Position should have 18 legal moves")
      (is (every? string? result) "All results should be strings"))))
