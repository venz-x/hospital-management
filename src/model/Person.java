package model;

import enums.Gender;
import interfaces.Identifiable;
import interfaces.Persistable;
import interfaces.Searchable;

/**
 * Abstract base class of everybody in the hospital.
 *
 * Demonstrates:
 *   - encapsulation (all fields private, reached through getters/setters)
 *   - abstraction  (getRole() and extraFields() have no body here)
 *   - a template method (toRecord() is written once and asks the
 *     subclass for its own extra columns)
 */
public abstract class Person
        implements Identifiable, Searchable, Persistable {

    /** Column separator used by every record in the data files. */
    public static final String SEP = "|";

    private final int id;
    private String name;
    private String phone;
    private Gender gender;

    protected Person(int id, String name, String phone, Gender gender) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.gender = gender;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /** Every subclass answers this differently: polymorphism. */
    public abstract String getRole();

    /** Columns that only the subclass knows about. */
    protected abstract String[] extraFields();

    /**
     * Template method: the common columns are written here once, the
     * subclass only supplies what is special about it.
     */
    @Override
    public String toRecord() {

        StringBuilder line = new StringBuilder();

        line.append(id).append(SEP)
            .append(name).append(SEP)
            .append(phone).append(SEP)
            .append(gender.name());

        for (String field : extraFields()) {
            line.append(SEP).append(field);
        }

        return line.toString();
    }

    /**
     * Default search: name, phone and role.
     * Subclasses override this and call super.matches(...) so the common
     * behaviour is not written three times.
     */
    @Override
    public boolean matches(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String needle = keyword.toLowerCase();

        return name.toLowerCase().contains(needle)
                || phone.toLowerCase().contains(needle)
                || getRole().toLowerCase().contains(needle);
    }

    /** Handy while testing from the console. */
    public void displayInfo() {
        System.out.println("[" + getRole() + "] #" + id
                + " " + name + " (" + gender.getLabel() + ") " + phone);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name;
    }
}
