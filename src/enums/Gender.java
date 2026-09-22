package enums;

/**
 * Enum with a constructor, a private field and behaviour of its own.
 * toString() is overridden so the constant can be dropped straight into
 * a Swing JComboBox and still show a human friendly label.
 */
public enum Gender {

    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
