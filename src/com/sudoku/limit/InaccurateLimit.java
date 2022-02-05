package com.sudoku.limit;

import com.sudoku.Sudoku;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class InaccurateLimit implements SudokuLimit {

    private static final Map<Sudoku, Predicate<Sudoku>> inaccurateLimits = new HashMap<>();

    @Override
    public boolean mustNotLimit(Sudoku sudoku) {
        return false;
    }

    @Override
    public void clear() {

    }

    public void addInaccurateLimit(Sudoku sudoku) {
        inaccurateLimits.putIfAbsent(sudoku, beTestedSudoku -> {
            if (beTestedSudoku.getX() == sudoku.getX()) {
                return beTestedSudoku.getRow() != sudoku.getRow()
                        && beTestedSudoku.getCol() != sudoku.getCol()
                        && beTestedSudoku.getGroup() != sudoku.getGroup();
            } else {
                return true;
            }
        });
    }

    public boolean limit(List<Sudoku> sudokus, Sudoku beTestedSudoku) {
        boolean result = true;
        for (Sudoku sudoku : sudokus) {
            result = inaccurateLimits.get(sudoku).test(beTestedSudoku);
            if (!result) {
                break;
            }
        }
        return result;
    }
}
