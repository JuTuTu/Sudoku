package com.sudoku;

import com.sudoku.limit.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.sudoku.SudokuDuplicator.copyList;

public class SudokuCalculator {

    private static final int[] BASE = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final List<Sudoku> total;
    private final Map<Integer, List<Integer>> groups = new HashMap<>();

    private final RowLimit rowLimit = new RowLimit();
    private final ColLimit colLimit = new ColLimit();
    private final GroupLimit groupLimit = new GroupLimit();
    private final InaccurateLimit inaccurateLimit = new InaccurateLimit();
    private final SudokuPrinter printer = new SudokuPrinter();
    private final SudokuSnapshot snapshot = new SudokuSnapshot();
    private static final int LOOP_FACTOR = 50;

    {
        groups.put(1, Arrays.asList(2, 3, 4, 7));
        groups.put(2, Arrays.asList(1, 3, 5, 8));
        groups.put(3, Arrays.asList(1, 2, 6, 9));
        groups.put(4, Arrays.asList(1, 5, 6, 7));
        groups.put(5, Arrays.asList(2, 4, 6, 8));
        groups.put(6, Arrays.asList(3, 4, 5, 9));
        groups.put(7, Arrays.asList(1, 4, 8, 9));
        groups.put(8, Arrays.asList(2, 5, 7, 9));
        groups.put(9, Arrays.asList(3, 6, 7, 8));
    }

    public SudokuCalculator(List<Sudoku> total) {
        this.total = total;
    }

    private boolean findAndSetOneInTotal() {
        setLimits(total);
        boolean hasOne = false;
        List<Sudoku> oneList;
        for (int number : BASE) {
            oneList = new ArrayList<>(this.findOne(number, total));
            if (oneList.size() > 0) {
                hasOne = true;
                oneList.forEach(t -> {
                    t.setFlag(SudokuPrinter.STAR);
                    total.set(getSameOneIndex(t, total), t);
                });
                setBasicLimits(oneList);
            }
        }
        return hasOne;
    }

    private List<Sudoku> findOnlyOne(List<Sudoku> sudokuList) {
        List<Sudoku> result = new ArrayList<>();
        BiConsumer<Integer, List<Sudoku>> consumer = (k, v) -> {
            if (v.stream().filter(t -> t.getX() == 0).count() == 1) {
                Optional<Sudoku> optional = v.stream().filter(t -> t.getX() == 0).findFirst();
                if (optional.isPresent()) {
                    Sudoku sudoku = optional.get();
                    result.add(new Sudoku(sudoku.getRow(), sudoku.getCol(), sudoku.getGroup(),
                            Arrays.stream(BASE).sum() - v.stream().mapToInt(Sudoku::getX).sum()));
                }
            }
        };
        sudokuList.stream().collect(Collectors.groupingBy(Sudoku::getGroup)).forEach(consumer);
        sudokuList.stream().collect(Collectors.groupingBy(Sudoku::getRow)).forEach(consumer);
        sudokuList.stream().collect(Collectors.groupingBy(Sudoku::getCol)).forEach(consumer);
        return new ArrayList<>(result);
    }

    private void setBasicLimits(List<Sudoku> sudokus) {
        sudokus.forEach(sudoku -> {
            if (sudoku.getX() != 0) {
                rowLimit.addMustNotRowLimit(sudoku);
                colLimit.addMustNotColLimit(sudoku);
                groupLimit.addMustNotGroupLimit(sudoku);
            }
        });
    }

    private void setLimits(List<Sudoku> sudokus) {
        for (int number : BASE) {
            int size;
            List<List<Sudoku>> doubleList = new ArrayList<>();
            do {
                size = doubleList.size();
                doubleList = findDouble(number, sudokus);
                setDoubleLimits(number, doubleList);
                if (doubleList.size() > 1) {
                    for (int i = 0; i < doubleList.size(); i++) {
                        for (int j = i + 1; j < doubleList.size(); j++) {
                            if (doubleList.get(i).get(0).equalsRow(doubleList.get(j).get(0)) && doubleList.get(i).get(1).equalsRow(doubleList.get(j).get(1))) {
                                int crossGroup = findCrossGroup(doubleList.get(i).get(0).getGroup(), doubleList.get(j).get(0).getGroup());
                                rowLimit.addMustNotRowGroupLimit(new Sudoku(doubleList.get(i).get(0).getRow(), 0, crossGroup, doubleList.get(i).get(0).getX()));
                                rowLimit.addMustNotRowGroupLimit(new Sudoku(doubleList.get(i).get(1).getRow(), 0, crossGroup, doubleList.get(i).get(1).getX()));
                            } else if (doubleList.get(i).get(0).equalsCol(doubleList.get(j).get(0)) && doubleList.get(i).get(1).equalsCol(doubleList.get(j).get(1))) {
                                int crossGroup = findCrossGroup(doubleList.get(i).get(0).getGroup(), doubleList.get(j).get(0).getGroup());
                                colLimit.addMustNotColGroupLimit(new Sudoku(0, doubleList.get(i).get(0).getCol(), crossGroup, doubleList.get(i).get(0).getX()));
                                colLimit.addMustNotColGroupLimit(new Sudoku(0, doubleList.get(i).get(1).getCol(), crossGroup, doubleList.get(i).get(1).getX()));
                            }
                        }
                    }
                }
            } while (doubleList.size() != size);
        }
    }

    private void setDoubleLimits(int number, List<List<Sudoku>> doubleList) {
        doubleList.forEach(sudokus -> {
            if (sudokus.get(0).getRow() == sudokus.get(1).getRow()) {
                rowLimit.addMustNotRowGroupLimitByGroup(total, sudokus.get(0), number);
            } else if (sudokus.get(0).getCol() == sudokus.get(1).getCol()) {
                colLimit.addMustNotColGroupLimitByGroup(total, sudokus.get(0), number);
            }
        });
    }

    private List<Sudoku> findOne(int number, List<Sudoku> sudokuList) {
        List<Sudoku> result = new ArrayList<>();
        Map<Integer, List<Sudoku>> map = getAllCouldPlaceSites(number, sudokuList);
        map.forEach((group, sudokus) -> {
            if (sudokus.size() == 1) {
                result.add(sudokus.get(0));
            }
        });
        return result;
    }

    private List<Sudoku> findInaccurateOne(List<Sudoku> sudokuList, SudokuTree sudokuTree) {
        List<Sudoku> result = new ArrayList<>();
        for (int number : BASE) {
            Map<Integer, List<Sudoku>> map = findInaccurateMap(sudokuList, sudokuTree, number);
            map.forEach((group, sudokus) -> {
                if (sudokus.size() == 1) {
                    if (result.stream().noneMatch(sudoku -> sudoku.getRow() == sudokus.get(0).getRow() && sudoku.getCol() == sudokus.get(0).getCol() && sudoku.getGroup() == sudokus.get(0).getGroup())) {
                        result.add(sudokus.get(0));
                    }
                }
            });
        }
        return result;
    }

    private List<List<Sudoku>> findInaccurateDouble(List<Sudoku> sudokuList, SudokuTree sudokuTree) {
        List<List<Sudoku>> result = new ArrayList<>();
        for (int number : BASE) {
            Map<Integer, List<Sudoku>> map = findInaccurateMap(sudokuList, sudokuTree, number);
            map.forEach((group, sudokus) -> {
                if (sudokus.size() == 2) {
                    result.add(sudokus);
                }
            });
        }
        return result;
    }

    private Map<Integer, List<Sudoku>> findInaccurateMap(List<Sudoku> sudokuList, SudokuTree sudokuTree, int number) {
        return sudokuList.stream().filter(sudoku -> sudoku.getX() == 0).map(sudoku -> new Sudoku(sudoku.getRow(), sudoku.getCol(), sudoku.getGroup(), number)).filter(sudoku -> rowLimit.mustNotLimit(sudoku) && colLimit.mustNotLimit(sudoku) && groupLimit.mustNotLimit(sudoku)
                && inaccurateLimit.limit(sudokuTree.getAllChainLimit(), sudoku)).collect(Collectors.groupingBy(Sudoku::getGroup));
    }

    private List<List<Sudoku>> findDouble(int number, List<Sudoku> sudokuList) {
        List<List<Sudoku>> result = new ArrayList<>();
        Map<Integer, List<Sudoku>> map = getAllCouldPlaceSites(number, sudokuList);
        map.forEach((group, sudokus) -> {
            if (sudokus.size() == 2) {
                result.add(sudokus);
            }
        });
        return result;
    }

    private Map<Integer, List<Sudoku>> getAllCouldPlaceSites(int number, List<Sudoku> sudokuList) {
        return sudokuList.stream().filter(sudoku -> sudoku.getX() == 0)
                .map(sudoku -> new Sudoku(sudoku.getRow(), sudoku.getCol(), sudoku.getGroup(), number))
                .filter(sudoku -> rowLimit.mustNotLimit(sudoku) && colLimit.mustNotLimit(sudoku) && groupLimit.mustNotLimit(sudoku))
                .collect(Collectors.groupingBy(Sudoku::getGroup));
    }

    private int findCrossGroup(int group1, int group2) {
        Optional<Integer> optional = groups.get(group2).stream().filter(t -> groups.get(group1).contains(t)).findFirst();
        return optional.orElse(-1);
    }

    private int getSameOneIndex(Sudoku sd, List<Sudoku> sudokuList) {
        return sudokuList.indexOf(new Sudoku(sd.getRow(), sd.getCol(), sd.getGroup(), 0));
    }

    public void compute() {
        setBasicLimits(total);
        boolean hasOne;
        do {
            hasOne = findAndSetOneInTotal();
            printer.printSudoku(total);
        } while (hasOne);
        if (end()) {
            return;
        }
        List<List<Sudoku>> doubleList = new ArrayList<>();
        for (int number : BASE) {
            doubleList.addAll(findDouble(number, total));
        }
        for (List<Sudoku> doubleSudoku : doubleList) {
            SudokuTree sudokuTree = new SudokuTree();
            sudokuTree.insertNode(doubleSudoku.get(0), doubleSudoku.get(1));
            snapshot.createSnapshot(Objects.hash(doubleSudoku.get(0), doubleSudoku.get(1)), total);
            sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getLeft());
            List<Sudoku> sudokus = copyList(total);
            sudokuTree.getCurrentSudoku().setFlag(SudokuPrinter.FIRST_QUESTION_MARK);
            sudokus.set(getSameOneIndex(sudokuTree.getCurrentSudoku(), sudokus), sudokuTree.getCurrentSudoku());
            SudokuStatus reasoningResult = reasoning(sudokus, sudokuTree);
            if (reasoningResult == SudokuStatus.OVER) {
                printer.printSudoku(total);
                return;
            } else if (reasoningResult == SudokuStatus.THIS_ITERATOR_OVER) {
                sudokuTree.changeCurrentNode(sudokuTree.getROOT().getRight());
                sudokus = snapshot.getSnapshot(sudokuTree.getBrotherHashCode());
                sudokuTree.getCurrentSudoku().setFlag(SudokuPrinter.FIRST_QUESTION_MARK);
                sudokus.set(getSameOneIndex(sudokuTree.getCurrentSudoku(), sudokus), sudokuTree.getCurrentSudoku());
                reasoningResult = reasoning(sudokus, sudokuTree);
                if (reasoningResult == SudokuStatus.OVER) {
                    printer.printSudoku(total);
                    return;
                }
            }
        }
        System.out.println("BAD LUCK!");
    }

    public SudokuStatus reasoning(List<Sudoku> sudokus, SudokuTree sudokuTree) {
        Integer loopCounter = sudokuTree.incrementLoopCounter(sudokuTree.getCurrentNode().getParent());
        if (loopCounter >= LOOP_FACTOR) {
            sudokuTree.resetLoopCounter(sudokuTree.getCurrentNode().getParent());
            return SudokuStatus.TOO_MANY_LOOPS;
        }
        List<Sudoku> oneList = findInaccurateOne(sudokus, sudokuTree);
        if (!oneList.isEmpty()) {
            for (Sudoku sudoku : oneList) {
                sudoku.setFlag(SudokuPrinter.QUESTION_MARK);
                sudokus.set(getSameOneIndex(sudoku, sudokus), sudoku);
                sudokuTree.extendChain(sudoku);
            }
            if (!verify(sudokus)) {
                return SudokuStatus.FIND_ERROR;
            } else {
                printer.printSudoku(sudokus);
                return reasoning(sudokus, sudokuTree);
            }
        } else {
            printer.printSudoku(sudokus);
            if (!verify(sudokus)) {
                return SudokuStatus.FIND_ERROR;
            } else if (endReasoning(sudokus)) {
                total.clear();
                total.addAll(copyList(sudokus));
                return SudokuStatus.OVER;
            } else {
                List<List<Sudoku>> doubleList = findInaccurateDouble(sudokus, sudokuTree);
                for (List<Sudoku> doubleSudoku : doubleList) {
                    sudokuTree.insertNode(doubleSudoku.get(0), doubleSudoku.get(1));
                    snapshot.createSnapshot(Objects.hash(doubleSudoku.get(0), doubleSudoku.get(1)), sudokus);
                    sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getLeft());
                    sudokus = snapshot.getSnapshot(Objects.hash(doubleSudoku.get(0), doubleSudoku.get(1)));
                    sudokuTree.getCurrentSudoku().setFlag(SudokuPrinter.FIRST_QUESTION_MARK);
                    sudokus.set(getSameOneIndex(sudokuTree.getCurrentSudoku(), sudokus), sudokuTree.getCurrentSudoku());
                    SudokuStatus reasoningResult = reasoning(sudokus, sudokuTree);
                    if (reasoningResult == SudokuStatus.OVER) {
                        return SudokuStatus.OVER;
                    } else if (reasoningResult == SudokuStatus.TOO_MANY_LOOPS) {
                        sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                        return SudokuStatus.THIS_ITERATOR_OVER;
                    } else if (reasoningResult == SudokuStatus.UNILATERAL_ITERATOR_OVER || reasoningResult == SudokuStatus.FIND_ERROR) {
                        if (sudokuTree.getCurrentNode().getParent().getLeft() == sudokuTree.getCurrentNode()) {
                            if (sudokuTree.getCurrentNode().getParent().getRight() == null) {
                                sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                                return SudokuStatus.THIS_ITERATOR_OVER;
                            }
                            sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent().getRight());
                            sudokus = snapshot.getSnapshot(sudokuTree.getBrotherHashCode());
                            sudokuTree.getCurrentSudoku().setFlag(SudokuPrinter.FIRST_QUESTION_MARK);
                            sudokus.set(getSameOneIndex(sudokuTree.getCurrentSudoku(), sudokus), sudokuTree.getCurrentSudoku());
                            reasoningResult = reasoning(sudokus, sudokuTree);
                            if (reasoningResult == SudokuStatus.OVER) {
                                return SudokuStatus.OVER;
                            } else if (reasoningResult == SudokuStatus.TOO_MANY_LOOPS) {
                                sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                                return SudokuStatus.TOO_MANY_LOOPS;
                            } else if (doubleSudoku == doubleList.get(doubleList.size() - 1)) {
                                sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                                return SudokuStatus.THIS_ITERATOR_OVER;
                            } else {
                                sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                                sudokus = snapshot.getSnapshot(sudokuTree.getBrotherHashCode());
                                sudokus.set(getSameOneIndex(sudokuTree.getCurrentSudoku(), sudokus), sudokuTree.getCurrentSudoku());
                            }
                        }
                    } else if (reasoningResult == SudokuStatus.THIS_ITERATOR_OVER) {
                        if (doubleSudoku == doubleList.get(doubleList.size() - 1)) {
                            sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                            return SudokuStatus.THIS_ITERATOR_OVER;
                        } else {
                            sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent().getRight());
                            sudokus = snapshot.getSnapshot(sudokuTree.getBrotherHashCode());
                            sudokuTree.getCurrentSudoku().setFlag(SudokuPrinter.FIRST_QUESTION_MARK);
                            sudokus.set(getSameOneIndex(sudokuTree.getCurrentSudoku(), sudokus), sudokuTree.getCurrentSudoku());
                            reasoningResult = reasoning(sudokus, sudokuTree);
                            if (reasoningResult == SudokuStatus.OVER) {
                                return SudokuStatus.OVER;
                            } else if (reasoningResult == SudokuStatus.TOO_MANY_LOOPS) {
                                sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                                return SudokuStatus.TOO_MANY_LOOPS;
                            } else if (doubleSudoku == doubleList.get(doubleList.size() - 1)) {
                                sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                                return SudokuStatus.THIS_ITERATOR_OVER;
                            } else {
                                sudokuTree.changeCurrentNode(sudokuTree.getCurrentNode().getParent());
                                sudokus = snapshot.getSnapshot(sudokuTree.getBrotherHashCode());
                                sudokus.set(getSameOneIndex(sudokuTree.getCurrentSudoku(), sudokus), sudokuTree.getCurrentSudoku());
                            }
                        }
                    }
                }
            }
        }
        return SudokuStatus.UNILATERAL_ITERATOR_OVER;
    }

    public boolean verify(List<Sudoku> sudokus) {
        boolean result = true;
        List<Sudoku> temp = sudokus.stream().filter(t -> t.getX() != 0).collect(Collectors.toList());
        for (Sudoku sudoku : temp) {
            if (temp.stream().anyMatch(t -> !t.equals(sudoku) && (t.equalsRow(sudoku) || t.equalsCol(sudoku)))) {
                sudoku.setFlag(SudokuPrinter.ERROR);
                printer.printSudoku(sudokus);
                return false;
            }
        }
        List<Sudoku> onlyOneList = findOnlyOne(sudokus);
        for (Sudoku sudoku : onlyOneList) {
            result = rowLimit.mustNotLimit(sudoku) && colLimit.mustNotLimit(sudoku) && groupLimit.mustNotLimit(sudoku);
            if (!result) {
                sudoku.setFlag(SudokuPrinter.ERROR);
                sudokus.set(getSameOneIndex(sudoku, sudokus), sudoku);
                printer.printSudoku(sudokus);
                break;
            }
        }
        return result;
    }

    private boolean end() {
        return total.stream().noneMatch(t -> t.getX() == 0);
    }

    private boolean endReasoning(List<Sudoku> temp) {
        return temp.stream().noneMatch(t -> t.getX() == 0);
    }

    enum SudokuStatus {
        OVER,
        THIS_ITERATOR_OVER,
        TOO_MANY_LOOPS,
        FIND_ERROR,
        UNILATERAL_ITERATOR_OVER
    }
}
