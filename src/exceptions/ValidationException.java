package exceptions;

/** Thrown when the user typed something the system cannot accept. */
public class ValidationException extends HospitalException {

    public ValidationException(String message) {
        super(message, "Invalid input");
    }
}
