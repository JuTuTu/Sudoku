package com.sudoku;

import com.sudoku.limit.InaccurateLimit;

import java.util.*;

public class SudokuTree {

    private final Node ROOT;
    private Node currentNode;
    // [1,2]
    private final List<Node> sudokuChain;
    // <1, [1,1,2,2]>
    private final Map<Node, List<Sudoku>> chainMap;
    private final Map<Node, Integer> chainLoopCounter;
    private final InaccurateLimit inaccurateLimit;

    public SudokuTree() {
        ROOT = new Node(new Sudoku(0,0,0,0), null, null, null);
        currentNode = ROOT;
        chainMap = new HashMap<>();
        sudokuChain = new ArrayList<>();
        inaccurateLimit = new InaccurateLimit();
        chainLoopCounter = new HashMap<>();
    }

    public Node getROOT() {
        return ROOT;
    }

    public void insertNode(Sudoku left, Sudoku right) {
        currentNode.left = new Node(left, currentNode, null, null);
        currentNode.right = new Node(right, currentNode, null, null);
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public Sudoku getCurrentSudoku() {
        return currentNode.getData();
    }

    public void changeCurrentNode(Node newPosition) {
        if (newPosition == this.currentNode.left) {
            sudokuChain.add(newPosition);
        } else if (newPosition == this.currentNode.parent.right) {
            sudokuChain.set(sudokuChain.size() - 1, newPosition);
        } else if (newPosition == this.ROOT.right) {
            sudokuChain.clear();
            sudokuChain.add(newPosition);
        } else if (newPosition == this.currentNode.parent) {
            sudokuChain.remove(this.currentNode);
        }
        this.currentNode = newPosition;
        constructChainMap(newPosition);
    }

    public void constructChainMap(Node node) {
        List<Sudoku> list = new ArrayList<>();
        list.add(node.getData());
        chainMap.put(node, list);
        inaccurateLimit.addInaccurateLimit(node.getData());
    }

    public void extendChain(Sudoku sudoku) {
        chainMap.get(sudokuChain.get(sudokuChain.size() - 1)).add(sudoku);
        inaccurateLimit.addInaccurateLimit(sudoku);
    }

    public List<Sudoku> getAllChainLimit() {
        List<Sudoku> result = new ArrayList<>();
        for (Node node : sudokuChain) {
            result.addAll(chainMap.get(node));
        }
        return result;
    }

    public int getBrotherHashCode() {
        return Objects.hash(currentNode.getParent().getLeft().getData(), currentNode.getParent().getRight().getData());
    }

    public Integer incrementLoopCounter(Node node) {
        return chainLoopCounter.merge(node, 1, Integer::sum);
    }

    public void resetLoopCounter(Node node) {
        chainLoopCounter.put(node, 0);
    }

    public Node getNodeBySudoku(Sudoku sudoku) {
        for (Map.Entry<Node, List<Sudoku>> entry : chainMap.entrySet()) {
            if (entry.getValue().contains(sudoku)) {
                return entry.getKey();
            }
        }
        return ROOT;
    }

    class Node {
        private Sudoku data;
        private Node parent;
        private Node left;
        private Node right;

        public Sudoku getData() {
            return data;
        }

        public Node getParent() {
            return parent;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public Node(Sudoku data, Node parent, Node left, Node right) {
            this.data = data;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return data.equals(node.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }
}
