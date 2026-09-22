package ui;

import enums.Gender;
import enums.Shift;
import model.Nurse;
import service.NurseService;

import javax.swing.JComboBox;
import javax.swing.JTextField;

public class NursePanel extends EntityPanel<Nurse> {

    private final NurseService nurseService;

    public NursePanel(NurseService nurseService) {

        super(nurseService,
                "Nurses",
                "Manage nursing staff and their duty shifts");

        this.nurseService = nurseService;

        refresh();
    }

    @Override
    protected String[] columnNames() {
        return new String[] {
                "ID", "Name", "Phone", "Gender", "Shift", "Qualification"
        };
    }

    @Override
    protected Object[] toRow(Nurse nurse) {

        return new Object[] {
                nurse.getId(),
                nurse.getName(),
                nurse.getPhone(),
                nurse.getGender().getLabel(),
                nurse.getShift().getLabel(),
                nurse.getQualification()
        };
    }

    @Override
    protected String describe(Nurse nurse) {
        return "nurse " + nurse.getName();
    }

    @Override
    protected void openCreateDialog() {

        FormPanel form = new FormPanel();

        JTextField name = form.addText("Name", "");
        JTextField phone = form.addText("Phone", "");
        JComboBox<Gender> gender =
                form.addCombo("Gender", Gender.values(), Gender.FEMALE);
        JComboBox<Shift> shift =
                form.addCombo("Shift", Shift.values(), Shift.MORNING);
        JTextField qualification = form.addText("Qualification", "");

        if (!form.showDialog(this, "Add nurse")) {
            return;
        }

        Nurse created = nurseService.createNurse(
                name.getText(),
                phone.getText(),
                (Gender) gender.getSelectedItem(),
                (Shift) shift.getSelectedItem(),
                qualification.getText());

        refresh();
        info("Nurse " + created.getName()
                + " was added with id " + created.getId() + ".");
    }

    @Override
    protected void openEditDialog(Nurse nurse) {

        FormPanel form = new FormPanel();

        form.addReadOnly("Nurse id", String.valueOf(nurse.getId()));

        JTextField name = form.addText("Name", nurse.getName());
        JTextField phone = form.addText("Phone", nurse.getPhone());
        JComboBox<Gender> gender =
                form.addCombo("Gender", Gender.values(), nurse.getGender());
        JComboBox<Shift> shift =
                form.addCombo("Shift", Shift.values(), nurse.getShift());
        JTextField qualification =
                form.addText("Qualification", nurse.getQualification());

        if (!form.showDialog(this, "Edit nurse")) {
            return;
        }

        nurseService.updateNurse(
                nurse.getId(),
                name.getText(),
                phone.getText(),
                (Gender) gender.getSelectedItem(),
                (Shift) shift.getSelectedItem(),
                qualification.getText());

        refresh();
        info("Nurse " + nurse.getName() + " was updated.");
    }

    @Override
    protected void deleteItem(Nurse nurse) {
        nurseService.deleteNurse(nurse.getId());
    }
}
