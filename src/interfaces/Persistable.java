package interfaces;

/**
 * Every object that can write itself as one line of a text file.
 *
 * The file layer only depends on this interface, never on the concrete
 * model class, so a single method saves doctors, nurses, patients or
 * medicines (polymorphism).
 */
public interface Persistable {

    /** One pipe separated line, without the line break. */
    String toRecord();
}
