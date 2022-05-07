package com.botga4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static com.botga4.Window.Game;
import static com.botga4.Window.Menu;

public class ColorFlip {
    private static String input;
    private static boolean exit = false;
    private static Window window = Menu;
    private static final Scanner consoleScanner = new Scanner(System.in);
    private static final File file = new File("savedGame.txt");
    private static Scanner fileScanner;
    private static FileWriter fileWriter;
    private static boolean[][] board;
    private static int size;
    private static final Random random = new Random();
    private static int stepCount = 0;

    public static void run() {
        initialize();
        while (!exit) {
            switch (window) {
                case Menu -> showMenu(); // only writes to console
                case Game -> showGame(); // only writes to console
            }
            handleCommand();
        }
    }

    private static void initialize() {
        try {
            welcome();
            fileScanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            handleFileNotFound();
        }
    }

    private static void welcome() {
        System.out.println("Welcome to Color Flip!");
    }

    private static void handleFileNotFound() {
        System.err.println("fileScanner error: loading disabled.");
    }

    private static void showMenu() {
        System.out.println("""
        List of commands:
        General:
            start -> Start a new game.
            load  -> Load saved game.
            exit  -> Exit program.
        In-Game:
            save  -> Save current game.
            stop  -> Stop current game.
            x y   -> Flip all elements of row #x and column #y.""");
    }

    private static void showGame() {
        System.out.println("Board content:");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j]) {
                    System.out.print('O');
                } else {
                    System.out.print('X');
                }
            }
            System.out.println();
        }
    }

    private static void handleCommand() {
        getInput();
        switch (input) {
            default -> handleDefault();
            case "start" -> handleStart();
            case "load" -> handleLoad();
            case "save" -> handleSave();
            case "stop" -> handleStop();
            case "exit" -> handleExit();
        }
    }

    private static void getInput() {
        System.out.println("System ready for new command...");
        input = consoleScanner.nextLine();
    }

    private static void handleDefault() {
        try {
            handleFlip();
        } catch (BadCommandException e) {
            handleBadCommand();
        }
    }

    private static void handleFlip() {
        int[] xy = getCoordinates();
        flipByCoordinates(xy);
        stepCount++;
        checkWin();
    }

    private static int[] getCoordinates() {
        List<String> coordinates = Arrays.asList(input.split(" "));
        if (coordinates.size() != 2) {
            throw new BadCommandException();
        }
        int x = Integer.parseInt(coordinates.get(0));
        int y = Integer.parseInt(coordinates.get(1));
        if (x < 1 || size < x || y < 1 || size < y) {
            throw new BadCommandException();
        }
        return new int[]{x-1, y-1};
    }

    private static void flipByCoordinates(int[] xy) {
        int x = xy[0];
        int y = xy[1];
        for (int i = 0; i < size; i++) {
            board[x][i] = !board[x][i];
            board[i][y] = !board[i][y];
        }
        board[x][y] = !board[x][y];
    }

    private static void checkWin() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] != board[0][0]) {
                    return;
                }
            }
        }
        win();
    }

    private static void win() {
        System.out.println("Congratulations, you won in " + stepCount
                + " steps!");
        window = Menu;
    }

    private static void handleBadCommand() {
        System.out.println("Command not valid. No changes applied.");
    }

    private static void handleStart() {
        getSizeInput();
        window = Game;
        try {
            generateNewBoard();
        } catch (NumberFormatException | BadCommandException e) {
            handleBadCommand();
        }
    }

    private static void getSizeInput() {
        System.out.println("Set a board size:");
        input = consoleScanner.nextLine();
    }

    private static void generateNewBoard() {
        size = Integer.parseInt(input);
        board = new boolean[size][size];
        if (size % 2 == 0) {
            generateNewEvenBoard();
        } else {
            generateNewOddBoard();
        }
        stepCount = 0;
        checkWin();
    }

    private static void generateNewEvenBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = random.nextBoolean();
            }
        }
    }

    private static void generateNewOddBoard() {
        generateTrueBoard();
        shuffleTrueBoard();
    }

    private static void generateTrueBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = true;
            }
        }
    }

    private static void shuffleTrueBoard() {
        int floor = size/2 + 1;
        int ceil = (int) Math.ceil(Math.pow(size, 1.5));
        int iterationCount = random.nextInt(floor, ceil);
        for (int i = 0; i < iterationCount; i++) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            flipByCoordinates(new int[]{x, y});
        }
    }

    private static void handleLoad() {
        if (noExistingSave()) return;
        generateSavedBoard();
        fileScanner.close();
        try {
            fileScanner = new Scanner(file);
        } catch (IOException e) {
            handleFileNotFound();
        }
    }

    private static boolean noExistingSave() {
        if (!fileScanner.hasNextLine()) {
            System.err.println("No game to load.");
            return true;
        }
        return false;
    }

    private static void generateSavedBoard() {
        window = Game;
        size = Integer.parseInt(fileScanner.nextLine());
        stepCount = Integer.parseInt(fileScanner.nextLine());
        board = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            String line = fileScanner.nextLine();
            for (int j = 0; j < size; j++) {
                switch (line.charAt(j)) {
                    case 'O' -> board[i][j] = true;
                    case 'X' -> board[i][j] = false;
                }
            }
        }
    }

    private static void handleSave() {
        if (window != Game) {
            handleBadCommand();
        } else {
            reopenFileWriter();
            exportBoard();
        }
    }

    private static void reopenFileWriter() {
        try {
            if (fileWriter != null) {
                fileWriter.close();
            }
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
            System.err.println("IOException.");
        }
    }

    private static void exportBoard() {
        writeToFile(size + "\n");
        writeToFile(stepCount + "\n");
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j]) {
                    writeToFile("O");
                } else {
                    writeToFile("X");
                }
            }
            writeToFile("\n");
        }
    }

    private static void writeToFile(String str) {
        try {
            fileWriter.write(str);
            fileWriter.flush();
        } catch (IOException e) {
            System.err.println("IOException");
        }
    }

    private static void handleStop() {
        if (window != Game) {
            handleBadCommand();
        } else {
            window = Menu;
        }
    }

    private static void handleExit() {
        exit = true;
    }
}
