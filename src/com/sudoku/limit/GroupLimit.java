package com.sudoku.limit;

import com.sudoku.Sudoku;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GroupLimit implements SudokuLimit {

    private static final List<Predicate<Sudoku>> groupMustNotLimits = new ArrayList<>();

    @Override
    public boolean mustNotLimit(Sudoku sudoku) {
        boolean result = true;
        for (Predicate<Sudoku> p : groupMustNotLimits) {
            result = result && p.test(sudoku);
        }
        return result;
    }

    @Override
    public void clear() {
        groupMustNotLimits.clear();
    }

    public void addMustNotGroupLimit(Sudoku sudoku) {
        groupMustNotLimits.add(beTestedSudoku -> {
            if (beTestedSudoku.getX() == sudoku.getX()) {
                return beTestedSudoku.getGroup() != sudoku.getGroup();
            } else {
                return true;
            }
        });
    }
}
