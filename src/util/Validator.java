package util;

import exceptions.ValidationException;

/**
 * Static helper methods used by the service layer before an object is
 * created. Keeping the checks here means every service validates the
 * same way instead of repeating the same if-statements.
 */
public final class Validator {

    /** The character that separates columns in the data files. */
    private static final String SEPARATOR = "|";

    private Validator() {
    }

    public static String requireText(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required");
        }

        String text = value.trim();

        if (text.contains(SEPARATOR)) {
            throw new ValidationException(
                    fieldName + " cannot contain the character |"
            );
        }

        if (text.length() > 60) {
            throw new ValidationException(
                    fieldName + " cannot be longer than 60 characters"
            );
        }

        return text;
    }

    public static String requireName(String value) {

        String name = requireText(value, "Name");

        if (name.length() < 2) {
            throw new ValidationException(
                    "Name must be at least 2 characters long"
            );
        }

        return name;
    }

    public static String requirePhone(String value) {

        String phone = requireText(value, "Phone");

        if (!phone.matches("[0-9+\\-\\s]{6,20}")) {
            throw new ValidationException(
                    "Phone must be 6-20 characters and contain digits only"
            );
        }

        return phone;
    }

    public static int requireInt(String value, String fieldName) {

        String text = requireText(value, fieldName);

        try {
            return Integer.parseInt(text);

        } catch (NumberFormatException e) {
            throw new ValidationException(
                    fieldName + " must be a whole number"
            );
        }
    }

    public static int requirePositive(String value, String fieldName) {

        int number = requireInt(value, fieldName);

        if (number < 0) {
            throw new ValidationException(fieldName + " cannot be negative");
        }

        return number;
    }

    public static double requirePrice(String value) {

        String text = requireText(value, "Price");

        double price;

        try {
            price = Double.parseDouble(text);

        } catch (NumberFormatException e) {
            throw new ValidationException("Price must be a number");
        }

        if (price < 0) {
            throw new ValidationException("Price cannot be negative");
        }

        return price;
    }

    /**
     * Generic enum conversion. <T extends Enum<T>> is what allows one
     * method to work for Gender, DepartmentType, PatientStatus, ...
     */
    public static <T extends Enum<T>> T requireEnum(
            String value,
            Class<T> type,
            String fieldName) {

        String text = requireText(value, fieldName);

        try {
            return Enum.valueOf(type, text.trim().toUpperCase());

        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                    "\"" + text + "\" is not a valid " + fieldName
            );
        }
    }

    /** A combo box can never be empty, but it can still be null. */
    public static <T> T requireChoice(T value, String fieldName) {

        if (value == null) {
            throw new ValidationException("Please choose a " + fieldName);
        }

        return value;
    }
}
