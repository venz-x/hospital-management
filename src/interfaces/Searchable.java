package interfaces;

/** Anything the search box on a Swing panel can filter. */
public interface Searchable {

    boolean matches(String keyword);
}
