package service;

import exceptions.NotFoundException;
import interfaces.Identifiable;
import interfaces.Searchable;
import model.Hospital;
import storage.DataStore;

import java.util.List;
import java.util.Optional;

/**
 * Everything the four services have in common.
 *
 * Abstraction: subclasses must say what they manage (findAll, label).
 * Inheritance: search(), persist() and require() are written once here.
 */
public abstract class BaseService<T extends Identifiable & Searchable> {

    protected final Hospital hospital;
    protected final DataStore store;

    protected BaseService(Hospital hospital, DataStore store) {
        this.hospital = hospital;
        this.store = store;
    }

    /** The word used in error messages, e.g. "Doctor". */
    public abstract String entityName();

    public abstract List<T> findAll();

    /** One search for all four entity types, through the interface. */
    public List<T> search(String keyword) {

        return findAll()
                .stream()
                .filter(item -> item.matches(keyword))
                .toList();
    }

    /** Writes the whole database back to disk after a change. */
    protected void persist() {
        store.saveAll(hospital);
    }

    protected T require(Optional<T> found, int id) {

        return found.orElseThrow(
                () -> new NotFoundException(entityName(), id));
    }
}
