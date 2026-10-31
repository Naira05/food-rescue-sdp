package MVC.view;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Small console helper shared by every screen. Reading from a Scanner (not System.in directly)
 * lets a test feed a scripted list of answers; with echo on, the answers are printed so the
 * transcript reads like a real session.
 */
public class ConsoleIO {
    private final Scanner in;
    private final boolean echo;

    public ConsoleIO(Scanner in, boolean echo) {
        this.in = in;
        this.echo = echo;
    }

    /** Console that reads the keyboard. */
    public static ConsoleIO keyboard() {
        return new ConsoleIO(new Scanner(System.in), false);
    }

    /** Console that reads answers from a text (one per line) and echoes them. */
    public static ConsoleIO scripted(String... lines) {
        return new ConsoleIO(new Scanner(String.join("\n", lines) + "\n"), true);
    }

    public void println(String text) {
        System.out.println(text);
    }

    /** Prints "label: " and returns the typed line, or null when the input has ended. */
    public String prompt(String label) {
        System.out.print(label + ": ");
        if (!in.hasNextLine()) {
            System.out.println();
            return null;
        }
        String line = in.nextLine();
        if (echo) {
            System.out.println(line);
        }
        return line;
    }

    public int promptInt(String label) {
        String text = prompt(label);
        try {
            return Integer.parseInt(String.valueOf(text).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a whole number");
        }
    }

    public double promptDouble(String label) {
        String text = prompt(label);
        try {
            return Double.parseDouble(String.valueOf(text).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a number");
        }
    }

    public LocalDate promptDate(String label) {
        String text = prompt(label);
        try {
            return LocalDate.parse(String.valueOf(text).trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use the format yyyy-MM-dd");
        }
    }
}
