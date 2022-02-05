package com.sudoku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SudokuSnapshot {

    private final Map<Integer, List<Sudoku>> snapshots;

    public SudokuSnapshot() {
        this.snapshots = new HashMap<>();
    }

    public void createSnapshot(int snapshotCode, List<Sudoku> sudokus) {
        snapshots.put(snapshotCode, SudokuDuplicator.copyList(sudokus));
    }

    public List<Sudoku> getSnapshot(int snapshotCode) {
        return SudokuDuplicator.copyList(snapshots.get(snapshotCode));
    }
}
