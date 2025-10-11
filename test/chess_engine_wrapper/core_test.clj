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

(deftest test-get-next-positions-standard-starting-position
  (testing "Getting next positions from standard starting position with all 20 specific FENs"
    (let [start-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          result (core/get-next-positions start-fen)
          ;; All 20 possible positions after one move from starting position
          expected-positions #{
            ;; Pawn moves (one square forward)
            "rnbqkbnr/pppppppp/8/8/8/P7/1PPPPPPP/RNBQKBNR b KQkq - 0 1"  ; a3
            "rnbqkbnr/pppppppp/8/8/8/1P6/P1PPPPPP/RNBQKBNR b KQkq - 0 1"  ; b3
            "rnbqkbnr/pppppppp/8/8/8/2P5/PP1PPPPP/RNBQKBNR b KQkq - 0 1"  ; c3
            "rnbqkbnr/pppppppp/8/8/8/3P4/PPP1PPPP/RNBQKBNR b KQkq - 0 1"  ; d3
            "rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR b KQkq - 0 1"  ; e3
            "rnbqkbnr/pppppppp/8/8/8/5P2/PPPPP1PP/RNBQKBNR b KQkq - 0 1"  ; f3
            "rnbqkbnr/pppppppp/8/8/8/6P1/PPPPPP1P/RNBQKBNR b KQkq - 0 1"  ; g3
            "rnbqkbnr/pppppppp/8/8/8/7P/PPPPPPP1/RNBQKBNR b KQkq - 0 1"  ; h3
            ;; Pawn moves (two squares forward) - Stockfish omits en passant target when no capture is possible
            "rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR b KQkq - 0 1"  ; a4
            "rnbqkbnr/pppppppp/8/8/1P6/8/P1PPPPPP/RNBQKBNR b KQkq - 0 1"  ; b4
            "rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq - 0 1"  ; c4
            "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1"  ; d4
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"  ; e4
            "rnbqkbnr/pppppppp/8/8/5P2/8/PPPPP1PP/RNBQKBNR b KQkq - 0 1"  ; f4
            "rnbqkbnr/pppppppp/8/8/6P1/8/PPPPPP1P/RNBQKBNR b KQkq - 0 1"  ; g4
            "rnbqkbnr/pppppppp/8/8/7P/8/PPPPPPP1/RNBQKBNR b KQkq - 0 1"  ; h4
            ;; Knight moves
            "rnbqkbnr/pppppppp/8/8/8/N7/PPPPPPPP/R1BQKBNR b KQkq - 1 1"   ; Na3
            "rnbqkbnr/pppppppp/8/8/8/2N5/PPPPPPPP/R1BQKBNR b KQkq - 1 1"   ; Nc3
            "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1"   ; Nf3
            "rnbqkbnr/pppppppp/8/8/8/7N/PPPPPPPP/RNBQKB1R b KQkq - 1 1"}] ; Nh3
      (is (= 20 (count result)) "Starting position should have 20 legal moves")
      (is (every? string? result) "All results should be strings")
      ;; Assert each expected position is in the result
      (doseq [expected expected-positions]
        (is (contains? (set result) expected) 
            (str "Result should contain position: " expected))))))

(deftest test-get-position-value-startpos
  (testing "Getting position value from starting position"
    (let [start-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          result (core/get-position-value start-fen 1000)]
      (is (some? result) "Should return a result")
      (is (number? (:score-cp result)) "Should return a centipawn score")
      (is (string? (:best-move result)) "Should return a best move")
      (is (re-matches #"[a-h][1-8][a-h][1-8][qrbn]?" (:best-move result))
          "Best move should be in UCI format"))))

(deftest test-get-position-value-favorable-position
  (testing "Getting position value from a position favorable to white"
    ;; Position after 1.e4 e5 2.Nf3 Nc6 3.Bb5 (Ruy Lopez)
    (let [ruy-lopez-fen "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3"
          result (core/get-position-value ruy-lopez-fen 1000)]
      (is (some? result) "Should return a result")
      (is (number? (:score-cp result)) "Should return a centipawn score")
      (is (string? (:best-move result)) "Should return a best move"))))

(deftest test-get-position-value-with-custom-engine
  (testing "Getting position value with custom engine path"
    (let [start-fen "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
          result (core/get-position-value start-fen 1000 "stockfish")]
      (is (some? result) "Should return a result")
      (is (number? (:score-cp result)) "Should return a centipawn score"))))
