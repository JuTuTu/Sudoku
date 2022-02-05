package com.sudoku.limit;

import com.sudoku.Sudoku;

public interface SudokuLimit {

    boolean mustNotLimit(Sudoku sudoku);

    void clear();
}
