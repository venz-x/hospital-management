package exceptions;

/** Thrown when a record with the requested id does not exist. */
public class NotFoundException extends HospitalException {

    public NotFoundException(String what, int id) {
        super(what + " with id " + id + " was not found", "Record not found");
    }

    public NotFoundException(String message) {
        super(message, "Record not found");
    }
}
