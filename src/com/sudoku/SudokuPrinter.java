package com.sudoku;

import java.util.List;
import java.util.stream.Collectors;

public class SudokuPrinter {

    public static final String QUESTION_MARK = "?";
    public static final String FIRST_QUESTION_MARK = "$";
    public static final String STAR = "*";
    public static final String ERROR = "!";

    public void printSudoku(List<Sudoku> list) {
        print(list.stream().map(t -> t.getX() + t.getFlag()).collect(Collectors.toList()));
        System.out.println("----------------------------------------------");
    }

    private void print(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if ((i + 1) % 3 == 0) {
                System.out.print(list.get(i) + "      ");
            } else {
                System.out.print(list.get(i) + "    ");
            }
            if ((i + 1) % 9 == 0) {
                System.out.println();
            }
            if ((i + 1) % 27 == 0) {
                System.out.println();
            }
        }
    }

}
