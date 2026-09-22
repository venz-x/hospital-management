package enums;

public enum Shift {

    MORNING("Morning"),
    EVENING("Evening"),
    NIGHT("Night");

    private final String label;

    Shift(String label) {
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
