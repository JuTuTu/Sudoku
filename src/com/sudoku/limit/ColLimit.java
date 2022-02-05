package com.sudoku.limit;

import com.sudoku.Sudoku;

import java.util.*;
import java.util.function.Predicate;

public class ColLimit implements SudokuLimit {

    private static final List<Predicate<Sudoku>> colMustNotLimits = new ArrayList<>();
    private static final Map<Integer, List<Integer>> colGroups = new HashMap<>();

    static {
        colGroups.put(1, Arrays.asList(4, 7));
        colGroups.put(2, Arrays.asList(5, 8));
        colGroups.put(3, Arrays.asList(6, 9));
        colGroups.put(4, Arrays.asList(1, 7));
        colGroups.put(5, Arrays.asList(2, 8));
        colGroups.put(6, Arrays.asList(3, 9));
        colGroups.put(7, Arrays.asList(1, 4));
        colGroups.put(8, Arrays.asList(2, 5));
        colGroups.put(9, Arrays.asList(3, 6));
    }

    @Override
    public boolean mustNotLimit(Sudoku sudoku) {
        boolean result = true;
        for (Predicate<Sudoku> p : colMustNotLimits) {
            result = result && p.test(sudoku);
        }
        return result;
    }

    @Override
    public void clear() {
        colMustNotLimits.clear();
    }

    public void addMustNotColLimit(Sudoku sudoku) {
        colMustNotLimits.add(beTestedSudoku -> {
            if (beTestedSudoku.getX() == sudoku.getX()) {
                return beTestedSudoku.getCol() != sudoku.getCol();
            } else {
                return true;
            }
        });
    }

    public void addMustNotColGroupLimit(Sudoku sudoku) {
        if (mustNotLimit(sudoku)) {
            colMustNotLimits.add(beTestedSudoku -> {
                if (beTestedSudoku.getGroup() == sudoku.getGroup() && beTestedSudoku.getX() == sudoku.getX()) {
                    return beTestedSudoku.getCol() != sudoku.getCol();
                } else {
                    return true;
                }
            });
        }
    }

    public void addMustNotColGroupLimitByGroup(List<Sudoku> total, Sudoku sudoku, int number) {
        colGroups.get(sudoku.getGroup()).forEach(crossGroup -> {
            if (total.stream().noneMatch(t -> t.getX() == number && t.getGroup() == crossGroup)) {
                addMustNotColGroupLimit(new Sudoku(0, sudoku.getCol(), crossGroup, number));
            }
        });
    }
}
