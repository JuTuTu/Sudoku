package com.sudoku;

import java.util.*;

public class Sudoku {

    private int row;
    private int col;
    private int group;
    private int x;
    private String flag = "";

    public Sudoku() {

    }

    public Sudoku(int row, int col, int group, int x) {
        super();
        this.row = row;
        this.col = col;
        this.group = group;
        this.x = x;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getGroup() {
        return group;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "ShuDu{" +
                "row=" + row +
                ", col=" + col +
                ", group=" + group +
                ", x=" + x +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sudoku sudoku = (Sudoku) o;
        return row == sudoku.row && col == sudoku.col && group == sudoku.group && x == sudoku.x;
    }

    public boolean equalsRow(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sudoku sudoku = (Sudoku) o;
        return row == sudoku.row && group != sudoku.group && x == sudoku.x;
    }

    public boolean equalsCol(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sudoku sudoku = (Sudoku) o;
        return col == sudoku.col && group != sudoku.group && x == sudoku.x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col, group, x);
    }

}
