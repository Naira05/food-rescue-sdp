package MVC.view;

import MVC.database.DataAccessException;

import java.util.List;

public abstract class ControlPanel {
    protected final ConsoleIO io;

    protected ControlPanel(ConsoleIO io) {
        this.io = io;
    }

    public final void run() {
        while (true) {
            displayDashboard();
            String choice = io.prompt("Choose");
            if (choice == null || choice.trim().equals("0")) {
                return;
            }
            try {
                handleChoice(choice.trim());
            } catch (IllegalArgumentException | IllegalStateException e) {
                io.println("Error: " + e.getMessage());
            } catch (DataAccessException e) {
                io.println("Database error: " + e.getMessage());
            }
        }
    }
    public abstract void displayDashboard();


    protected abstract void handleChoice(String choice);

    protected void printMenu(String title, List<String> options) {
        io.println("\n=== " + title + " ===");
        for (int i = 0; i < options.size(); i++) {
            io.println("  " + (i + 1) + ". " + options.get(i));
        }
        io.println("  0. Log out");
    }
}
