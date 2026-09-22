package model;

import enums.Gender;
import enums.Shift;

public class Nurse extends Person {

    private Shift shift;
    private String qualification;

    public Nurse(
            int id,
            String name,
            String phone,
            Gender gender,
            Shift shift,
            String qualification) {

        super(id, name, phone, gender);

        this.shift = shift;
        this.qualification = qualification;
    }

    @Override
    public String getRole() {
        return "Nurse";
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    @Override
    protected String[] extraFields() {

        return new String[] {
                shift.name(),
                qualification
        };
    }

    @Override
    public boolean matches(String keyword) {

        if (super.matches(keyword)) {
            return true;
        }

        String needle = keyword.toLowerCase();

        return qualification.toLowerCase().contains(needle)
                || shift.getLabel().toLowerCase().contains(needle);
    }
}
