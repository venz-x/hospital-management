package ui;

import enums.BloodGroup;
import enums.Gender;
import enums.PatientStatus;
import model.Medicine;
import model.Nurse;
import model.Patient;
import service.MedicineService;
import service.NurseService;
import service.PatientService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

/**
 * The Patients tab.
 *
 * Patients are the only entity connected to the others, so this panel
 * has four extra buttons: assign a nurse, prescribe a medicine, return
 * a medicine to the pharmacy, and discharge.
 */
public class PatientPanel extends EntityPanel<Patient> {

    private final PatientService patientService;
    private final NurseService nurseService;
    private final MedicineService medicineService;

    public PatientPanel(
            PatientService patientService,
            NurseService nurseService,
            MedicineService medicineService) {

        super(patientService,
                "Patients",
                "Admit patients, assign a nurse and prescribe from the pharmacy");

        this.patientService = patientService;
        this.nurseService = nurseService;
        this.medicineService = medicineService;

        refresh();
    }

    @Override
    protected String[] columnNames() {
        return new String[] {
                "ID", "Name", "Phone", "Gender", "Disease",
                "Blood", "Status", "Nurse", "Medicines"
        };
    }

    @Override
    protected Object[] toRow(Patient patient) {

        Nurse nurse = patient.getAssignedNurse();

        return new Object[] {
                patient.getId(),
                patient.getName(),
                patient.getPhone(),
                patient.getGender().getLabel(),
                patient.getDisease(),
                patient.getBloodGroup().getLabel(),
                patient.getStatus().getLabel(),
                nurse == null ? "-" : nurse.getName(),
                patient.medicineSummary()
        };
    }

    @Override
    protected String describe(Patient patient) {
        return "patient " + patient.getName();
    }

    @Override
    protected List<JButton> extraButtons() {

        List<JButton> buttons = new ArrayList<>();

        JButton assign = UiTheme.button("Assign nurse", UiTheme.PRIMARY_DARK);
        assign.addActionListener(event -> openAssignNurseDialog());

        JButton prescribe = UiTheme.button("Prescribe", UiTheme.PRIMARY_DARK);
        prescribe.addActionListener(event -> openPrescribeDialog());

        JButton returnMedicine =
                UiTheme.button("Return medicine", UiTheme.MUTED);
        returnMedicine.addActionListener(event -> openReturnMedicineDialog());

        JButton discharge = UiTheme.button("Discharge", UiTheme.MUTED);
        discharge.addActionListener(event -> dischargeSelected());

        buttons.add(assign);
        buttons.add(prescribe);
        buttons.add(returnMedicine);
        buttons.add(discharge);

        return buttons;
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    @Override
    protected void openCreateDialog() {

        FormPanel form = new FormPanel();

        JTextField name = form.addText("Name", "");
        JTextField phone = form.addText("Phone", "");
        JComboBox<Gender> gender =
                form.addCombo("Gender", Gender.values(), Gender.MALE);
        JTextField disease = form.addText("Disease", "");
        JComboBox<BloodGroup> bloodGroup = form.addCombo(
                "Blood group", BloodGroup.values(), BloodGroup.O_POSITIVE);
        JComboBox<PatientStatus> status = form.addCombo(
                "Status", PatientStatus.values(), PatientStatus.OUTPATIENT);

        if (!form.showDialog(this, "Register patient")) {
            return;
        }

        Patient created = patientService.createPatient(
                name.getText(),
                phone.getText(),
                (Gender) gender.getSelectedItem(),
                disease.getText(),
                (BloodGroup) bloodGroup.getSelectedItem(),
                (PatientStatus) status.getSelectedItem());

        refresh();
        info("Patient " + created.getName()
                + " was registered with id " + created.getId() + ".");
    }

    @Override
    protected void openEditDialog(Patient patient) {

        FormPanel form = new FormPanel();

        form.addReadOnly("Patient id", String.valueOf(patient.getId()));

        JTextField name = form.addText("Name", patient.getName());
        JTextField phone = form.addText("Phone", patient.getPhone());
        JComboBox<Gender> gender =
                form.addCombo("Gender", Gender.values(), patient.getGender());
        JTextField disease = form.addText("Disease", patient.getDisease());
        JComboBox<BloodGroup> bloodGroup = form.addCombo(
                "Blood group", BloodGroup.values(), patient.getBloodGroup());
        JComboBox<PatientStatus> status = form.addCombo(
                "Status", PatientStatus.values(), patient.getStatus());

        if (!form.showDialog(this, "Edit patient")) {
            return;
        }

        patientService.updatePatient(
                patient.getId(),
                name.getText(),
                phone.getText(),
                (Gender) gender.getSelectedItem(),
                disease.getText(),
                (BloodGroup) bloodGroup.getSelectedItem(),
                (PatientStatus) status.getSelectedItem());

        refresh();
        info("Patient " + patient.getName() + " was updated.");
    }

    @Override
    protected void deleteItem(Patient patient) {
        patientService.deletePatient(patient.getId());
    }

    // ------------------------------------------------------------------
    // relationships
    // ------------------------------------------------------------------

    private void openAssignNurseDialog() {

        Patient patient = requireSelected();

        if (patient == null) {
            return;
        }

        runSafely(() -> {

            List<Nurse> nurses = nurseService.findAll();

            if (nurses.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "There are no nurses on file yet.\n"
                                + "Add one in the Nurses tab first.",
                        "No nurses",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            FormPanel form = new FormPanel();

            form.addReadOnly("Patient", patient.getName());

            JComboBox<Nurse> nurse = form.addNullableCombo(
                    "Nurse",
                    nurses,
                    patient.getAssignedNurse(),
                    "-- no nurse --");

            if (!form.showDialog(this, "Assign nurse")) {
                return;
            }

            Nurse chosen = (Nurse) nurse.getSelectedItem();

            patientService.assignNurse(patient.getId(), chosen);

            refresh();
            info(chosen == null
                    ? "The nurse was removed from " + patient.getName() + "."
                    : chosen.getName() + " is now looking after "
                            + patient.getName() + ".");
        });
    }

    private void openPrescribeDialog() {

        Patient patient = requireSelected();

        if (patient == null) {
            return;
        }

        runSafely(() -> {

            List<Medicine> medicines = medicineService.findAll();

            if (medicines.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "The pharmacy is empty.\n"
                                + "Add a medicine in the Pharmacy tab first.",
                        "No medicines",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            FormPanel form = new FormPanel();

            form.addReadOnly("Patient", patient.getName());

            JComboBox<Medicine> medicine = form.addCombo(
                    "Medicine",
                    medicines.toArray(new Medicine[0]),
                    medicines.get(0));

            JTextField quantity = form.addText("Quantity", "1");

            if (!form.showDialog(this, "Prescribe medicine")) {
                return;
            }

            Medicine chosen = (Medicine) medicine.getSelectedItem();

            patientService.prescribeMedicine(
                    patient.getId(), chosen, quantity.getText());

            refresh();
            info(quantity.getText() + " unit(s) of "
                    + (chosen == null ? "" : chosen.getName())
                    + " were given to " + patient.getName()
                    + " and removed from the pharmacy stock.");
        });
    }

    private void openReturnMedicineDialog() {

        Patient patient = requireSelected();

        if (patient == null) {
            return;
        }

        runSafely(() -> {

            List<Medicine> prescribed = patient.getMedicineList();

            if (prescribed.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        patient.getName() + " has no medicine on the chart.",
                        "Nothing to return",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            FormPanel form = new FormPanel();

            form.addReadOnly("Patient", patient.getName());

            JComboBox<Medicine> medicine = form.addCombo(
                    "Medicine",
                    prescribed.toArray(new Medicine[0]),
                    prescribed.get(0));

            if (!form.showDialog(this, "Return medicine to pharmacy")) {
                return;
            }

            Medicine chosen = (Medicine) medicine.getSelectedItem();

            if (chosen == null) {
                return;
            }

            patientService.removeMedicine(patient.getId(), chosen.getId());

            refresh();
            info(chosen.getName()
                    + " was taken off the chart and put back in stock.");
        });
    }

    private void dischargeSelected() {

        Patient patient = requireSelected();

        if (patient == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Discharge " + patient.getName() + "?\n"
                        + "The assigned nurse will also be released.",
                "Confirm discharge",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        runSafely(() -> {
            patientService.discharge(patient.getId());
            refresh();
            info(patient.getName() + " has been discharged.");
        });
    }
}
