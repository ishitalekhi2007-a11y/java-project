package com.restaurant.ui;

import java.math.BigDecimal;
import java.util.Scanner;

/** Re-prompts until the user enters a valid value. */
class Input {
    private final Scanner scanner;

    Input(Scanner scanner) {
        this.scanner = scanner;
    }

    String text(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                throw new EndOfInput();
            }
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
        }
    }

    /** Like text(), but an empty line returns the default. */
    String textOr(String prompt, String fallback) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new EndOfInput();
        }
        String line = scanner.nextLine().trim();
        return line.isEmpty() ? fallback : line;
    }

    BigDecimal amountOr(String prompt, BigDecimal fallback) {
        while (true) {
            String value = textOr(prompt, null);
            if (value == null) {
                return fallback;
            }
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number, e.g. 249.50");
            }
        }
    }

    int integer(String prompt) {
        while (true) {
            String value = text(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    BigDecimal amount(String prompt) {
        while (true) {
            String value = text(prompt);
            try {
                return new BigDecimal(value);
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a number, e.g. 249.50");
            }
        }
    }

    boolean yesNo(String prompt) {
        return text(prompt + " (y/n): ").toLowerCase().startsWith("y");
    }

    <E extends Enum<E>> E choice(String prompt, E[] values) {
        for (int i = 0; i < values.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, values[i]);
        }
        while (true) {
            int pick = integer(prompt);
            if (pick >= 1 && pick <= values.length) {
                return values[pick - 1];
            }
            System.out.println("  Choose 1-" + values.length + ".");
        }
    }

    static class EndOfInput extends RuntimeException {
    }
}
