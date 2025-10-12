# Engine Session Refactoring

This document describes the refactoring completed to enable persistent engine sessions.

## Summary

All engine functions (`get-next-positions` and `get-position-value`) now support both ephemeral and persistent engine sessions. The API is fully backward compatible while providing significant performance improvements for multiple operations.

## What Changed

### Core Functions Refactored

1. **`get-next-positions`** - Now accepts either:
   - A string path to UCI engine executable (creates ephemeral session)
   - An initialized engine instance (uses persistent session)

2. **`get-position-value`** - Now accepts either:
   - A string path to UCI engine executable (creates ephemeral session)
   - An initialized engine instance (uses persistent session)

### Implementation Details

Both functions use a simple type check to determine the parameter type:
- If `engine-or-path` is a map → treat it as an engine instance (persistent session)
- If `engine-or-path` is a string → treat it as a path (ephemeral session)

The persistent session path does NOT call `stop-engine`, allowing the engine to be reused across multiple function calls within a `with-engine` block.

## Benefits

### Performance
- **2-3x faster** for multiple operations (measured in examples)
- Avoids engine startup/shutdown overhead
- Single process handles multiple requests

### State Preservation
- Engine maintains internal state between requests
- More efficient analysis when working with related positions
- Better use of engine's hash tables and position cache

### Backward Compatibility
- All existing code continues to work without modification
- Existing tests pass without changes
- API signatures remain identical

## Usage Patterns

### Pattern 1: Ephemeral Session (Backward Compatible)
```clojure
;; Creates and destroys engine for each operation
(get-next-positions fen)
(get-next-positions fen "stockfish")
(get-position-value fen 1000)
(get-position-value fen 1000 "stockfish")
```

### Pattern 2: Persistent Session (New, More Efficient)
```clojure
;; Single engine for multiple operations
(with-engine "stockfish"
  (fn [engine]
    (get-next-positions fen engine)
    (get-position-value fen 1000 engine)
    ;; More operations with same engine...
    ))
```

### Pattern 3: Analyzing Game Progression
```clojure
(with-engine "stockfish"
  (fn [engine]
    (doseq [fen game-positions]
      (let [eval (get-position-value fen 500 engine)]
        (println "Score:" (:score-cp eval))))))
```

## Testing

### Test Coverage
- **5 new tests** added for persistent session functionality
- **26 new assertions** validating persistent session behavior
- **25 total tests** (20 original + 5 new)
- **196 total assertions** (170 original + 26 new)
- **All tests pass** with no failures or errors

### Test Categories
1. **Persistent session basic functionality**
   - `test-get-next-positions-with-persistent-session`
   - `test-get-position-value-with-persistent-session`

2. **Multiple operations with same session**
   - `test-multiple-operations-with-persistent-session`
   - `test-persistent-session-maintains-performance`

3. **Backward compatibility**
   - `test-backward-compatibility-string-path`
   - All original 20 tests continue to pass

## Examples

### New Example File
Created `example_persistent_session.clj` demonstrating:
1. Multiple operations with single engine session
2. Analyzing game progressions efficiently
3. Direct UCI access within a session
4. Performance comparison (persistent vs ephemeral)

Run with: `clojure -M -m example-persistent-session`

## Documentation Updates

### README.md
- Added new section: "Using Persistent Engine Sessions"
- Updated API reference for both functions
- Added examples showing both usage patterns
- Updated feature list to highlight persistent sessions
- Added performance notes (2-3x improvement)

### Function Docstrings
Both functions now document:
- Parameter accepts either engine instance or path
- Behavior difference between the two modes
- When to use each mode

## Migration Guide

### For Existing Users
**No changes required!** Your existing code continues to work exactly as before.

### To Use New Persistent Sessions
Simply pass an engine instance instead of a path:

```clojure
;; Before (still works)
(get-position-value fen 1000 "stockfish")

;; After (more efficient for multiple operations)
(with-engine "stockfish"
  (fn [engine]
    (get-position-value fen 1000 engine)))
```

## Files Changed

- **Modified:** `src/chess_engine_wrapper/core.clj` (+41/-26 lines)
  - Refactored `get-next-positions` to support both modes
  - Refactored `get-position-value` to support both modes
  - Enhanced docstrings

- **Modified:** `test/chess_engine_wrapper/core_test.clj` (+70 lines)
  - Added 5 new test cases
  - Added 26 new assertions
  - Validates persistent session functionality

- **New:** `example_persistent_session.clj` (144 lines)
  - Comprehensive examples of persistent sessions
  - Performance comparison demonstration

- **Modified:** `README.md` (+105 lines)
  - New "Using Persistent Engine Sessions" section
  - Updated API reference
  - Enhanced examples

## Performance Measurements

From `example_persistent_session.clj` output:

```
Timing 5 operations with persistent session... 752 ms
Timing 5 operations with ephemeral sessions... 1745 ms

Persistent session saved ~993 ms (2.3x faster)
```

## Verification

All functionality verified:
- ✓ Ephemeral sessions work (backward compatible)
- ✓ Persistent sessions work (new feature)
- ✓ Multiple operations with persistent session
- ✓ All 25 tests pass
- ✓ Performance improvements confirmed
- ✓ Documentation updated and accurate

## Conclusion

This refactoring successfully addresses the requirement to support persistent engine sessions while maintaining 100% backward compatibility. The implementation is minimal, focused, and provides significant performance benefits for users who need to perform multiple engine operations.
