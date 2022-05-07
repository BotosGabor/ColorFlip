package com.botga4;

public class Solver {
    private static boolean[][] board;
    private static boolean[][] solution;
    private static int boardSize;
    private static int solutionSize;

    public static void printOddSolution(boolean[][] flipped) {
        solveOddBoard(flipped);
        printSolution();
    }

    public static void printEvenSolution(boolean[][] board) {
        solveEvenBoard(board);
        printSolution();
    }

    private static void printSolution() {
        System.out.println("Flips at places with value 1 are necessary.");
        for (int i = 0; i < solutionSize; i++) {
            for (int j = 0; j < solutionSize; j++) {
                System.out.print(solution[i][j]? '1' : '0');
            }
            System.out.println();
        }
    }

    private static void solveOddBoard(boolean[][] flipped) {
        solution = flipped;
        solutionSize = solution.length;
        invertIfNeeded();
    }

    public static void solveEvenBoard(boolean[][] board) {
        boardSize = board.length;
        solution = initializeSolution();
        Solver.board = board;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j]) {
                    updateSolution(i, j);
                }
            }
        }
        invertIfNeeded();
    }

    private static boolean[][] initializeSolution() {
        solutionSize = boardSize;
        boolean[][] solution = new boolean[solutionSize][solutionSize];
        for (int i = 0; i < solutionSize; i++) {
            for (int j = 0; j < solutionSize; j++) {
                solution[i][j] = false;
            }
        }
        return solution;
    }

    private static void updateSolution(int i, int j) {
        for (int k = 0; k < solutionSize; k++) {
            solution[i][k] = !solution[i][k];
            solution[k][j] = !solution[k][j];
        }
        solution[i][j] = !solution[i][j];
    }

    private static void invertIfNeeded() {
        int countTrue = 0;
        for (int i = 0; i < solutionSize; i++) {
            for (int j = 0; j < solutionSize; j++) {
                if (solution[i][j]) countTrue++;
            }
        }
        if (countTrue > solutionSize * solutionSize / 2) {
            invert();
        }
    }

    private static void invert() {
        for (int i = 0; i < solutionSize; i++) {
            for (int j = 0; j < solutionSize; j++) {
                solution[i][j] = !solution[i][j];
            }
        }
    }
}
