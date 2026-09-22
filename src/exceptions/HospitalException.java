package exceptions;

/**
 * Base class for every error this application throws on purpose.
 *
 * It carries a short title so the Swing layer can put any subclass into
 * a JOptionPane with a sensible heading, without a chain of instanceof
 * checks. That is polymorphism doing the dispatching for us.
 */
public class HospitalException extends RuntimeException {

    private final String title;

    public HospitalException(String message, String title) {
        super(message);
        this.title = title;
    }

    public HospitalException(String message, String title, Throwable cause) {
        super(message, cause);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
