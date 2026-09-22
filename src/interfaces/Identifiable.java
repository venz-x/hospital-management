package interfaces;

/**
 * Anything the hospital stores and looks up by id.
 * This is what lets Repository<T> be generic.
 */
public interface Identifiable {

    int getId();
}
