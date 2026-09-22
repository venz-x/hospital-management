package model;

import enums.BloodGroup;
import enums.Gender;
import enums.PatientStatus;
import exceptions.ValidationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Patient shows object relationships:
 *   - one Nurse may be assigned to the patient (association)
 *   - a map of prescribed Medicine objects with their quantity
 */
public class Patient extends Person {

    private String disease;
    private BloodGroup bloodGroup;
    private PatientStatus status;

    private Nurse assignedNurse;

    /** Which medicine was given to this patient, and how many units. */
    private final Map<Medicine, Integer> medicines = new LinkedHashMap<>();

    public Patient(
            int id,
            String name,
            String phone,
            Gender gender,
            String disease,
            BloodGroup bloodGroup,
            PatientStatus status) {

        super(id, name, phone, gender);

        this.disease = disease;
        this.bloodGroup = bloodGroup;
        this.status = status == null ? PatientStatus.OUTPATIENT : status;
    }

    @Override
    public String getRole() {
        return "Patient";
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public PatientStatus getStatus() {
        return status;
    }

    public void setStatus(PatientStatus status) {
        this.status = status;
    }

    public Nurse getAssignedNurse() {
        return assignedNurse;
    }

    public void assignNurse(Nurse nurse) {
        this.assignedNurse = nurse;
    }

    public void removeNurse() {
        this.assignedNurse = null;
    }

    /** A copy, so nobody can change the patient's chart from outside. */
    public Map<Medicine, Integer> getMedicines() {
        return new LinkedHashMap<>(medicines);
    }

    public List<Medicine> getMedicineList() {
        return new ArrayList<>(medicines.keySet());
    }

    public int getQuantityOf(Medicine medicine) {
        return medicines.getOrDefault(medicine, 0);
    }

    /**
     * Takes the medicine out of the pharmacy stock and writes it on the
     * patient's chart. reduceStock() may throw OutOfStockException and
     * we let that travel up to the Swing panel.
     */
    public void prescribe(Medicine medicine, int quantity) {

        if (medicine == null) {
            throw new ValidationException("Medicine is required");
        }

        medicine.reduceStock(quantity);

        medicines.merge(medicine, quantity, Integer::sum);
    }

    /**
     * Used only by the file loader: the saved stock already has the
     * prescription subtracted, so this must not touch the stock again.
     */
    public void restorePrescription(Medicine medicine, int quantity) {

        if (medicine != null && quantity > 0) {
            medicines.put(medicine, quantity);
        }
    }

    /** Gives the remaining units back to the pharmacy. */
    public boolean removeMedicine(int medicineId) {

        for (Medicine medicine : new ArrayList<>(medicines.keySet())) {

            if (medicine.getId() == medicineId) {

                int quantity = medicines.remove(medicine);
                medicine.addStock(quantity);

                return true;
            }
        }

        return false;
    }

    public void discharge() {
        this.status = PatientStatus.DISCHARGED;
        this.assignedNurse = null;
    }

    /** "Napa 500mg x6, Omeprazole 20mg x2" for the Swing table. */
    public String medicineSummary() {

        if (medicines.isEmpty()) {
            return "-";
        }

        List<String> parts = new ArrayList<>();

        for (Map.Entry<Medicine, Integer> entry : medicines.entrySet()) {
            parts.add(entry.getKey().getName() + " x" + entry.getValue());
        }

        return String.join(", ", parts);
    }

    @Override
    protected String[] extraFields() {

        return new String[] {
                disease,
                bloodGroup.name(),
                status.name(),
                assignedNurse == null ? "0" : String.valueOf(assignedNurse.getId()),
                prescriptionField()
        };
    }

    /** "4001:6;4003:2" - kept in one column of the patient record. */
    private String prescriptionField() {

        List<String> parts = new ArrayList<>();

        for (Map.Entry<Medicine, Integer> entry : medicines.entrySet()) {
            parts.add(entry.getKey().getId() + ":" + entry.getValue());
        }

        return String.join(";", parts);
    }

    @Override
    public boolean matches(String keyword) {

        if (super.matches(keyword)) {
            return true;
        }

        String needle = keyword.toLowerCase();

        return disease.toLowerCase().contains(needle)
                || status.getLabel().toLowerCase().contains(needle)
                || bloodGroup.getLabel().toLowerCase().contains(needle);
    }
}
