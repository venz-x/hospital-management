package exceptions;

/** Business rule: the pharmacy cannot give away what it does not have. */
public class OutOfStockException extends HospitalException {

    public OutOfStockException(String medicineName, int available) {
        super(
                "Not enough stock for " + medicineName
                        + " (only " + available + " left)",
                "Out of stock"
        );
    }
}
