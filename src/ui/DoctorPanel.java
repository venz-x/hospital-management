package ui;

import enums.DepartmentType;
import enums.Gender;
import model.Doctor;
import service.DoctorService;

import javax.swing.JComboBox;
import javax.swing.JTextField;

/** The Doctors tab. Everything else it needs comes from EntityPanel. */
public class DoctorPanel extends EntityPanel<Doctor> {

    private final DoctorService doctorService;

    public DoctorPanel(DoctorService doctorService) {

        super(doctorService,
                "Doctors",
                "Register doctors and keep their department details up to date");

        this.doctorService = doctorService;

        refresh();
    }

    @Override
    protected String[] columnNames() {
        return new String[] {
                "ID", "Name", "Phone", "Gender", "Specialization", "Department"
        };
    }

    @Override
    protected Object[] toRow(Doctor doctor) {

        return new Object[] {
                doctor.getId(),
                doctor.getName(),
                doctor.getPhone(),
                doctor.getGender().getLabel(),
                doctor.getSpecialization(),
                doctor.getDepartment().getLabel()
        };
    }

    @Override
    protected String describe(Doctor doctor) {
        return "doctor " + doctor.getName();
    }

    @Override
    protected void openCreateDialog() {

        FormPanel form = new FormPanel();

        JTextField name = form.addText("Name", "");
        JTextField phone = form.addText("Phone", "");
        JComboBox<Gender> gender =
                form.addCombo("Gender", Gender.values(), Gender.MALE);
        JTextField specialization = form.addText("Specialization", "");
        JComboBox<DepartmentType> department = form.addCombo(
                "Department",
                DepartmentType.values(),
                DepartmentType.GENERAL_MEDICINE);

        if (!form.showDialog(this, "Add doctor")) {
            return;
        }

        Doctor created = doctorService.createDoctor(
                name.getText(),
                phone.getText(),
                (Gender) gender.getSelectedItem(),
                specialization.getText(),
                (DepartmentType) department.getSelectedItem());

        refresh();
        info("Doctor " + created.getName()
                + " was added with id " + created.getId() + ".");
    }

    @Override
    protected void openEditDialog(Doctor doctor) {

        FormPanel form = new FormPanel();

        form.addReadOnly("Doctor id", String.valueOf(doctor.getId()));

        JTextField name = form.addText("Name", doctor.getName());
        JTextField phone = form.addText("Phone", doctor.getPhone());
        JComboBox<Gender> gender =
                form.addCombo("Gender", Gender.values(), doctor.getGender());
        JTextField specialization =
                form.addText("Specialization", doctor.getSpecialization());
        JComboBox<DepartmentType> department = form.addCombo(
                "Department", DepartmentType.values(), doctor.getDepartment());

        if (!form.showDialog(this, "Edit doctor")) {
            return;
        }

        doctorService.updateDoctor(
                doctor.getId(),
                name.getText(),
                phone.getText(),
                (Gender) gender.getSelectedItem(),
                specialization.getText(),
                (DepartmentType) department.getSelectedItem());

        refresh();
        info("Doctor " + doctor.getName() + " was updated.");
    }

    @Override
    protected void deleteItem(Doctor doctor) {
        doctorService.deleteDoctor(doctor.getId());
    }
}
