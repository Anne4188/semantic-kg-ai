package ngordnet.algs4;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class In {
    private Scanner scanner;

    public In(String filename) {
        try {
            scanner = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
    }

    public boolean hasNextLine() {
        return scanner.hasNextLine();
    }

    public String readLine() {
        return scanner.nextLine();
    }
}