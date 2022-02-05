package com.sudoku;

import java.util.ArrayList;
import java.util.List;

public class SudokuDuplicator {

    public static List<Sudoku> copyList(List<Sudoku> sudokuList) {
        List<Sudoku> result = new ArrayList<>(sudokuList.size());
        for (Sudoku sudoku : sudokuList) {
            result.add(new Sudoku(sudoku.getRow(), sudoku.getCol(), sudoku.getGroup(), sudoku.getX()));
        }
        return result;
    }
}
