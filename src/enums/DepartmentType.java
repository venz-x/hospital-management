package enums;

public enum DepartmentType {

    CARDIOLOGY("Cardiology"),
    NEUROLOGY("Neurology"),
    ORTHOPEDICS("Orthopedics"),
    PEDIATRICS("Pediatrics"),
    DERMATOLOGY("Dermatology"),
    GENERAL_MEDICINE("General Medicine"),
    SURGERY("Surgery"),
    EMERGENCY("Emergency");

    private final String label;

    DepartmentType(String label) {
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
