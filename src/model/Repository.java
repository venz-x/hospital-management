package model;

import interfaces.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A generic in-memory table.
 *
 * Because every model implements Identifiable, one class can store
 * doctors, nurses, patients and medicines instead of writing the same
 * add / find / remove code four times.
 */
public class Repository<T extends Identifiable> {

    private final List<T> items = new ArrayList<>();
    private int nextId;

    public Repository(int firstId) {
        this.nextId = firstId;
    }

    /** Ids are handed out by the store that owns the objects. */
    public int nextId() {
        return nextId++;
    }

    public void add(T item) {
        items.add(item);
        ensureNextIdAbove(item.getId());
    }

    /**
     * After loading a file the counter must continue from the highest
     * id that was read, otherwise the next new record would reuse an id.
     */
    public void ensureNextIdAbove(int id) {

        if (id >= nextId) {
            nextId = id + 1;
        }
    }

    public Optional<T> findById(int id) {

        for (T item : items) {

            if (item.getId() == id) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    public boolean remove(int id) {
        return items.removeIf(item -> item.getId() == id);
    }

    public void clear() {
        items.clear();
    }

    /** A copy, so callers cannot change the internal list directly. */
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public int count() {
        return items.size();
    }
}
