package model;

import java.util.List;
import java.util.Optional;

/**
 * The whole in-memory database of the application.
 *
 * Hospital owns four repositories (composition) and knows nothing about
 * Swing or about files. The storage layer reads and refills it.
 */
public class Hospital {

    private final String name;

    private final Repository<Doctor> doctors = new Repository<>(1001);
    private final Repository<Nurse> nurses = new Repository<>(2001);
    private final Repository<Patient> patients = new Repository<>(3001);
    private final Repository<Medicine> medicines = new Repository<>(4001);

    public Hospital(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // ---------------- doctors ----------------

    public int nextDoctorId() {
        return doctors.nextId();
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
    }

    public Optional<Doctor> findDoctor(int id) {
        return doctors.findById(id);
    }

    public List<Doctor> getDoctors() {
        return doctors.getAll();
    }

    public boolean removeDoctor(int id) {
        return doctors.remove(id);
    }

    // ---------------- nurses ----------------

    public int nextNurseId() {
        return nurses.nextId();
    }

    public void addNurse(Nurse nurse) {
        nurses.add(nurse);
    }

    public Optional<Nurse> findNurse(int id) {
        return nurses.findById(id);
    }

    public List<Nurse> getNurses() {
        return nurses.getAll();
    }

    /**
     * Removing a nurse also detaches her from every patient, otherwise a
     * patient would point at somebody who no longer works here.
     */
    public boolean removeNurse(int id) {

        for (Patient patient : patients.getAll()) {

            Nurse assigned = patient.getAssignedNurse();

            if (assigned != null && assigned.getId() == id) {
                patient.removeNurse();
            }
        }

        return nurses.remove(id);
    }

    // ---------------- patients ----------------

    public int nextPatientId() {
        return patients.nextId();
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    public Optional<Patient> findPatient(int id) {
        return patients.findById(id);
    }

    public List<Patient> getPatients() {
        return patients.getAll();
    }

    /** A deleted patient gives every unused unit back to the pharmacy. */
    public boolean removePatient(int id) {

        Optional<Patient> found = patients.findById(id);

        if (found.isEmpty()) {
            return false;
        }

        Patient patient = found.get();

        for (Medicine medicine : patient.getMedicineList()) {
            patient.removeMedicine(medicine.getId());
        }

        return patients.remove(id);
    }

    // ---------------- medicines ----------------

    public int nextMedicineId() {
        return medicines.nextId();
    }

    public void addMedicine(Medicine medicine) {
        medicines.add(medicine);
    }

    public Optional<Medicine> findMedicine(int id) {
        return medicines.findById(id);
    }

    public List<Medicine> getMedicines() {
        return medicines.getAll();
    }

    /**
     * Deleting a medicine also takes it off every patient's chart, so no
     * patient keeps pointing at something the pharmacy no longer has.
     */
    public boolean removeMedicine(int id) {

        for (Patient patient : patients.getAll()) {
            patient.removeMedicine(id);
        }

        return medicines.remove(id);
    }

    // ---------------- counters for the dashboard ----------------

    public int countDoctors() {
        return doctors.count();
    }

    public int countNurses() {
        return nurses.count();
    }

    public int countPatients() {
        return patients.count();
    }

    public int countMedicines() {
        return medicines.count();
    }

    public long countAdmittedPatients() {

        return patients.getAll()
                .stream()
                .filter(patient -> patient.getStatus().needsBed())
                .count();
    }

    public long countLowStockMedicines() {

        return medicines.getAll()
                .stream()
                .filter(medicine -> medicine.getQuantity() < 10)
                .count();
    }

    public double pharmacyStockValue() {

        return medicines.getAll()
                .stream()
                .mapToDouble(m -> m.getQuantity() * m.getPrice())
                .sum();
    }

    /** Wipes everything. Used just before the files are read back in. */
    public void clearAll() {
        doctors.clear();
        nurses.clear();
        patients.clear();
        medicines.clear();
    }
}
