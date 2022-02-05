package com.sudoku.limit;

import com.sudoku.Sudoku;

import java.util.*;
import java.util.function.Predicate;

public class RowLimit implements SudokuLimit {

    private static final List<Predicate<Sudoku>> rowMustNotLimits = new ArrayList<>();
    private static final Map<Integer, List<Integer>> rowGroups = new HashMap<>();

    static {
        rowGroups.put(1, Arrays.asList(2, 3));
        rowGroups.put(2, Arrays.asList(1, 3));
        rowGroups.put(3, Arrays.asList(1, 2));
        rowGroups.put(4, Arrays.asList(5, 6));
        rowGroups.put(5, Arrays.asList(4, 6));
        rowGroups.put(6, Arrays.asList(4, 5));
        rowGroups.put(7, Arrays.asList(8, 9));
        rowGroups.put(8, Arrays.asList(7, 9));
        rowGroups.put(9, Arrays.asList(7, 8));
    }

    @Override
    public boolean mustNotLimit(Sudoku sudoku) {
        boolean result = true;
        for (Predicate<Sudoku> p : rowMustNotLimits) {
            result = result && p.test(sudoku);
        }
        return result;
    }

    @Override
    public void clear() {
        rowMustNotLimits.clear();
    }

    public void addMustNotRowLimit(Sudoku sudoku) {
        rowMustNotLimits.add(beTestedSudoku -> {
            if (beTestedSudoku.getX() == sudoku.getX()) {
                return beTestedSudoku.getRow() != sudoku.getRow();
            } else {
                return true;
            }
        });
    }

    public void addMustNotRowGroupLimit(Sudoku sudoku) {
        if (mustNotLimit(sudoku)) {
            rowMustNotLimits.add(beTestedSudoku -> {
                if (beTestedSudoku.getGroup() == sudoku.getGroup() && beTestedSudoku.getX() == sudoku.getX()) {
                    return beTestedSudoku.getRow() != sudoku.getRow();
                } else {
                    return true;
                }
            });
        }
    }

    public void addMustNotRowGroupLimitByGroup(List<Sudoku> total, Sudoku sudoku, int number) {
        rowGroups.get(sudoku.getGroup()).forEach(crossGroup -> {
            if (total.stream().noneMatch(t -> t.getX() == number && t.getGroup() == crossGroup)) {
                addMustNotRowGroupLimit(new Sudoku(sudoku.getRow(), 0, crossGroup, number));
            }
        });
    }
}
