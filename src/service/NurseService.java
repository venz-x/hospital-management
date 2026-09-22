package service;

import enums.Gender;
import enums.Shift;
import exceptions.NotFoundException;
import model.Hospital;
import model.Nurse;
import storage.DataStore;
import util.Validator;

import java.util.List;

public class NurseService extends BaseService<Nurse> {

    public NurseService(Hospital hospital, DataStore store) {
        super(hospital, store);
    }

    @Override
    public String entityName() {
        return "Nurse";
    }

    @Override
    public List<Nurse> findAll() {
        return hospital.getNurses();
    }

    public Nurse getNurse(int id) {
        return require(hospital.findNurse(id), id);
    }

    public Nurse createNurse(
            String name,
            String phone,
            Gender gender,
            Shift shift,
            String qualification) {

        Nurse nurse = new Nurse(
                hospital.nextNurseId(),
                Validator.requireName(name),
                Validator.requirePhone(phone),
                Validator.requireChoice(gender, "gender"),
                Validator.requireChoice(shift, "shift"),
                Validator.requireText(qualification, "Qualification")
        );

        hospital.addNurse(nurse);
        persist();

        return nurse;
    }

    public Nurse updateNurse(
            int id,
            String name,
            String phone,
            Gender gender,
            Shift shift,
            String qualification) {

        Nurse nurse = getNurse(id);

        String validName = Validator.requireName(name);
        String validPhone = Validator.requirePhone(phone);
        Gender validGender = Validator.requireChoice(gender, "gender");
        Shift validShift = Validator.requireChoice(shift, "shift");
        String validQualification =
                Validator.requireText(qualification, "Qualification");

        nurse.setName(validName);
        nurse.setPhone(validPhone);
        nurse.setGender(validGender);
        nurse.setShift(validShift);
        nurse.setQualification(validQualification);

        persist();

        return nurse;
    }

    public void deleteNurse(int id) {

        if (!hospital.removeNurse(id)) {
            throw new NotFoundException("Nurse", id);
        }

        persist();
    }
}
