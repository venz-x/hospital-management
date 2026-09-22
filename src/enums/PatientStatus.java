package enums;

public enum PatientStatus {

    OUTPATIENT("Outpatient"),
    ADMITTED("Admitted"),
    DISCHARGED("Discharged");

    private final String label;

    PatientStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** A small piece of behaviour that belongs to the enum itself. */
    public boolean needsBed() {
        return this == ADMITTED;
    }

    @Override
    public String toString() {
        return label;
    }
}
