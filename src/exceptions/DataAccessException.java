package exceptions;

/**
 * Wraps the checked IOException thrown by the file layer so the rest of
 * the program can deal with one family of exceptions only.
 */
public class DataAccessException extends HospitalException {

    public DataAccessException(String message, Throwable cause) {
        super(message, "File error", cause);
    }

    public DataAccessException(String message) {
        super(message, "File error");
    }
}
