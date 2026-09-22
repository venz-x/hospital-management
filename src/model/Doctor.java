package model;

import enums.DepartmentType;
import enums.Gender;

/** A doctor. Inherits everything a Person has and adds two fields. */
public class Doctor extends Person {

    private String specialization;
    private DepartmentType department;

    public Doctor(
            int id,
            String name,
            String phone,
            Gender gender,
            String specialization,
            DepartmentType department) {

        super(id, name, phone, gender);

        this.specialization = specialization;
        this.department = department;
    }

    @Override
    public String getRole() {
        return "Doctor";
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public DepartmentType getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentType department) {
        this.department = department;
    }

    @Override
    protected String[] extraFields() {

        return new String[] {
                specialization,
                department.name()
        };
    }

    @Override
    public boolean matches(String keyword) {

        if (super.matches(keyword)) {
            return true;
        }

        String needle = keyword.toLowerCase();

        return specialization.toLowerCase().contains(needle)
                || department.getLabel().toLowerCase().contains(needle);
    }
}
