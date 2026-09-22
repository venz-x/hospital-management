package service;

import enums.BloodGroup;
import enums.Gender;
import enums.PatientStatus;
import exceptions.NotFoundException;
import model.Hospital;
import model.Medicine;
import model.Nurse;
import model.Patient;
import storage.DataStore;
import util.Validator;

import java.util.List;

/**
 * Patients are the only entity that talks to the other ones, so this
 * service also handles assigning a nurse and prescribing medicine.
 */
public class PatientService extends BaseService<Patient> {

    public PatientService(Hospital hospital, DataStore store) {
        super(hospital, store);
    }

    @Override
    public String entityName() {
        return "Patient";
    }

    @Override
    public List<Patient> findAll() {
        return hospital.getPatients();
    }

    public Patient getPatient(int id) {
        return require(hospital.findPatient(id), id);
    }

    public Patient createPatient(
            String name,
            String phone,
            Gender gender,
            String disease,
            BloodGroup bloodGroup,
            PatientStatus status) {

        Patient patient = new Patient(
                hospital.nextPatientId(),
                Validator.requireName(name),
                Validator.requirePhone(phone),
                Validator.requireChoice(gender, "gender"),
                Validator.requireText(disease, "Disease"),
                Validator.requireChoice(bloodGroup, "blood group"),
                Validator.requireChoice(status, "status")
        );

        hospital.addPatient(patient);
        persist();

        return patient;
    }

    public Patient updatePatient(
            int id,
            String name,
            String phone,
            Gender gender,
            String disease,
            BloodGroup bloodGroup,
            PatientStatus status) {

        Patient patient = getPatient(id);

        String validName = Validator.requireName(name);
        String validPhone = Validator.requirePhone(phone);
        Gender validGender = Validator.requireChoice(gender, "gender");
        String validDisease = Validator.requireText(disease, "Disease");
        BloodGroup validBlood = Validator.requireChoice(bloodGroup, "blood group");
        PatientStatus validStatus = Validator.requireChoice(status, "status");

        patient.setName(validName);
        patient.setPhone(validPhone);
        patient.setGender(validGender);
        patient.setDisease(validDisease);
        patient.setBloodGroup(validBlood);
        patient.setStatus(validStatus);

        persist();

        return patient;
    }

    public void deletePatient(int id) {

        if (!hospital.removePatient(id)) {
            throw new NotFoundException("Patient", id);
        }

        persist();
    }

    // ---------------- relationships ----------------

    /** Pass null to take the nurse off the patient. */
    public Patient assignNurse(int patientId, Nurse nurse) {

        Patient patient = getPatient(patientId);

        if (nurse == null) {
            patient.removeNurse();

        } else {

            // Make sure the nurse really is one of ours.
            Nurse onFile = hospital.findNurse(nurse.getId())
                    .orElseThrow(() ->
                            new NotFoundException("Nurse", nurse.getId()));

            patient.assignNurse(onFile);
        }

        persist();

        return patient;
    }

    public Patient prescribeMedicine(
            int patientId,
            Medicine medicine,
            String quantity) {

        Patient patient = getPatient(patientId);

        Medicine chosen = Validator.requireChoice(medicine, "medicine");
        int amount = Validator.requireInt(quantity, "Quantity");

        // May throw OutOfStockException, which the Swing panel shows.
        patient.prescribe(chosen, amount);
        persist();

        return patient;
    }

    public Patient removeMedicine(int patientId, int medicineId) {

        Patient patient = getPatient(patientId);

        if (!patient.removeMedicine(medicineId)) {
            throw new NotFoundException(
                    "This patient has no medicine with id " + medicineId);
        }

        persist();

        return patient;
    }

    public Patient discharge(int patientId) {

        Patient patient = getPatient(patientId);
        patient.discharge();

        persist();

        return patient;
    }
}
