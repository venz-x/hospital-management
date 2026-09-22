package service;

import enums.DepartmentType;
import enums.Gender;
import exceptions.NotFoundException;
import model.Doctor;
import model.Hospital;
import storage.DataStore;
import util.Validator;

import java.util.List;

/**
 * Business logic for doctors.
 *
 * The Swing panel only builds the form, this class only knows the
 * "doctor rules", Hospital only stores, DataStore only saves. That
 * separation is the point of the layers.
 */
public class DoctorService extends BaseService<Doctor> {

    public DoctorService(Hospital hospital, DataStore store) {
        super(hospital, store);
    }

    @Override
    public String entityName() {
        return "Doctor";
    }

    @Override
    public List<Doctor> findAll() {
        return hospital.getDoctors();
    }

    public Doctor getDoctor(int id) {
        return require(hospital.findDoctor(id), id);
    }

    /** CREATE - values come straight from the Swing form. */
    public Doctor createDoctor(
            String name,
            String phone,
            Gender gender,
            String specialization,
            DepartmentType department) {

        Doctor doctor = new Doctor(
                hospital.nextDoctorId(),
                Validator.requireName(name),
                Validator.requirePhone(phone),
                Validator.requireChoice(gender, "gender"),
                Validator.requireText(specialization, "Specialization"),
                Validator.requireChoice(department, "department")
        );

        hospital.addDoctor(doctor);
        persist();

        return doctor;
    }

    /** UPDATE */
    public Doctor updateDoctor(
            int id,
            String name,
            String phone,
            Gender gender,
            String specialization,
            DepartmentType department) {

        Doctor doctor = getDoctor(id);

        // Validate everything first, so a bad field cannot leave the
        // object half updated.
        String validName = Validator.requireName(name);
        String validPhone = Validator.requirePhone(phone);
        Gender validGender = Validator.requireChoice(gender, "gender");
        String validSpec = Validator.requireText(specialization, "Specialization");
        DepartmentType validDept = Validator.requireChoice(department, "department");

        doctor.setName(validName);
        doctor.setPhone(validPhone);
        doctor.setGender(validGender);
        doctor.setSpecialization(validSpec);
        doctor.setDepartment(validDept);

        persist();

        return doctor;
    }

    /** DELETE */
    public void deleteDoctor(int id) {

        if (!hospital.removeDoctor(id)) {
            throw new NotFoundException("Doctor", id);
        }

        persist();
    }
}
