package service;

import enums.BloodGroup;
import enums.DepartmentType;
import enums.Gender;
import enums.PatientStatus;
import enums.Shift;
import model.Medicine;
import model.Nurse;
import model.Patient;

/**
 * Fills the hospital with a few rows the first time the program is run,
 * so the tables are not empty during a demo. Pure static utility with
 * no state of its own.
 */
public final class SampleData {

    private SampleData() {
    }

    public static void load(
            DoctorService doctorService,
            NurseService nurseService,
            PatientService patientService,
            MedicineService medicineService) {

        doctorService.createDoctor(
                "Dr. Ayesha Rahman", "01711000001", Gender.FEMALE,
                "Interventional Cardiology", DepartmentType.CARDIOLOGY);

        doctorService.createDoctor(
                "Dr. Imran Hossain", "01711000002", Gender.MALE,
                "Paediatric Surgery", DepartmentType.SURGERY);

        doctorService.createDoctor(
                "Dr. Nusrat Jahan", "01711000003", Gender.FEMALE,
                "Child Health", DepartmentType.PEDIATRICS);

        Nurse mitu = nurseService.createNurse(
                "Mitu Akter", "01822000001", Gender.FEMALE,
                Shift.MORNING, "BSc in Nursing");

        nurseService.createNurse(
                "Rakib Hasan", "01822000002", Gender.MALE,
                Shift.NIGHT, "Diploma in Nursing");

        Medicine napa = medicineService.createMedicine(
                "Napa 500mg", "Beximco", "120", "1.20");

        medicineService.createMedicine(
                "Amoxicillin 250mg", "Square", "60", "6.50");

        medicineService.createMedicine(
                "Omeprazole 20mg", "Incepta", "40", "4.00");

        Patient karim = patientService.createPatient(
                "Karim Uddin", "01933000001", Gender.MALE,
                "Typhoid fever", BloodGroup.B_POSITIVE, PatientStatus.ADMITTED);

        patientService.createPatient(
                "Shila Begum", "01933000002", Gender.FEMALE,
                "Migraine", BloodGroup.O_POSITIVE, PatientStatus.OUTPATIENT);

        // Object relationships in action.
        patientService.assignNurse(karim.getId(), mitu);
        patientService.prescribeMedicine(karim.getId(), napa, "6");
    }
}
