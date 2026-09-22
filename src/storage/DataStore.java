package storage;

import enums.BloodGroup;
import enums.DepartmentType;
import enums.Gender;
import enums.PatientStatus;
import enums.Shift;
import exceptions.DataAccessException;
import interfaces.Persistable;
import model.Doctor;
import model.Hospital;
import model.Medicine;
import model.Nurse;
import model.Patient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * File based persistence.
 *
 * Every entity is written as one pipe separated line in its own text
 * file inside the data folder:
 *
 *   doctors.txt    id|name|phone|gender|specialization|department
 *   nurses.txt     id|name|phone|gender|shift|qualification
 *   medicines.txt  id|name|manufacturer|quantity|price
 *   patients.txt   id|name|phone|gender|disease|bloodGroup|status|nurseId|meds
 *
 * Writing is fully polymorphic: writeAll() only knows the Persistable
 * interface, so the same three lines save all four entity types. The
 * checked IOException is caught here and rethrown as our own
 * DataAccessException so the Swing layer only handles one family.
 */
public class DataStore {

    private static final String SEP = "\\|";
    private static final String COMMENT = "#";

    private final Path dataDir;
    private final Path doctorsFile;
    private final Path nursesFile;
    private final Path patientsFile;
    private final Path medicinesFile;

    /** Lines that could not be read are reported instead of crashing. */
    private final List<String> warnings = new ArrayList<>();

    public DataStore(String folder) {

        this.dataDir = Paths.get(folder);

        this.doctorsFile = dataDir.resolve("doctors.txt");
        this.nursesFile = dataDir.resolve("nurses.txt");
        this.patientsFile = dataDir.resolve("patients.txt");
        this.medicinesFile = dataDir.resolve("medicines.txt");
    }

    public DataStore() {
        this("data");
    }

    public Path getDataDir() {
        return dataDir;
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    /** True the very first time the program runs on this computer. */
    public boolean isEmpty() {
        return !Files.exists(doctorsFile)
                && !Files.exists(nursesFile)
                && !Files.exists(patientsFile)
                && !Files.exists(medicinesFile);
    }

    // ------------------------------------------------------------------
    // saving
    // ------------------------------------------------------------------

    /** Called after every create, update and delete. */
    public void saveAll(Hospital hospital) {

        try {
            Files.createDirectories(dataDir);

        } catch (IOException e) {
            throw new DataAccessException(
                    "Could not create the data folder: " + dataDir, e);
        }

        writeAll(doctorsFile, hospital.getDoctors(),
                "id|name|phone|gender|specialization|department");

        writeAll(nursesFile, hospital.getNurses(),
                "id|name|phone|gender|shift|qualification");

        writeAll(medicinesFile, hospital.getMedicines(),
                "id|name|manufacturer|quantity|price");

        writeAll(patientsFile, hospital.getPatients(),
                "id|name|phone|gender|disease|bloodGroup|status|nurseId|medicines");
    }

    /**
     * One method for all four entity types, because the list is typed as
     * "anything that can turn itself into a record".
     */
    private void writeAll(
            Path file,
            List<? extends Persistable> items,
            String header) {

        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            writer.write(COMMENT + " " + header);
            writer.newLine();

            for (Persistable item : items) {
                writer.write(item.toRecord());
                writer.newLine();
            }

        } catch (IOException e) {
            throw new DataAccessException(
                    "Could not save " + file.getFileName() + ": "
                            + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // loading
    // ------------------------------------------------------------------

    /**
     * Refills the hospital from disk. Medicines and nurses are read
     * first because a patient record points at them by id.
     */
    public void loadAll(Hospital hospital) {

        warnings.clear();
        hospital.clearAll();

        loadDoctors(hospital);
        loadNurses(hospital);
        loadMedicines(hospital);
        loadPatients(hospital);
    }

    private void loadDoctors(Hospital hospital) {

        for (String line : readLines(doctorsFile)) {

            try {
                String[] c = split(line, 6, "doctor");

                hospital.addDoctor(new Doctor(
                        Integer.parseInt(c[0]),
                        c[1],
                        c[2],
                        Gender.valueOf(c[3]),
                        c[4],
                        DepartmentType.valueOf(c[5])
                ));

            } catch (RuntimeException e) {
                warnings.add("doctors.txt: skipped a bad line (" + line + ")");
            }
        }
    }

    private void loadNurses(Hospital hospital) {

        for (String line : readLines(nursesFile)) {

            try {
                String[] c = split(line, 6, "nurse");

                hospital.addNurse(new Nurse(
                        Integer.parseInt(c[0]),
                        c[1],
                        c[2],
                        Gender.valueOf(c[3]),
                        Shift.valueOf(c[4]),
                        c[5]
                ));

            } catch (RuntimeException e) {
                warnings.add("nurses.txt: skipped a bad line (" + line + ")");
            }
        }
    }

    private void loadMedicines(Hospital hospital) {

        for (String line : readLines(medicinesFile)) {

            try {
                String[] c = split(line, 5, "medicine");

                hospital.addMedicine(new Medicine(
                        Integer.parseInt(c[0]),
                        c[1],
                        c[2],
                        Integer.parseInt(c[3]),
                        Double.parseDouble(c[4])
                ));

            } catch (RuntimeException e) {
                warnings.add("medicines.txt: skipped a bad line (" + line + ")");
            }
        }
    }

    private void loadPatients(Hospital hospital) {

        for (String line : readLines(patientsFile)) {

            try {
                String[] c = split(line, 9, "patient");

                Patient patient = new Patient(
                        Integer.parseInt(c[0]),
                        c[1],
                        c[2],
                        Gender.valueOf(c[3]),
                        c[4],
                        BloodGroup.valueOf(c[5]),
                        PatientStatus.valueOf(c[6])
                );

                int nurseId = Integer.parseInt(c[7]);

                if (nurseId > 0) {

                    Optional<Nurse> nurse = hospital.findNurse(nurseId);

                    if (nurse.isPresent()) {
                        patient.assignNurse(nurse.get());
                    } else {
                        warnings.add("Patient " + c[0]
                                + " referred to nurse " + nurseId
                                + ", who is no longer on file");
                    }
                }

                restorePrescriptions(hospital, patient, c[8]);

                hospital.addPatient(patient);

            } catch (RuntimeException e) {
                warnings.add("patients.txt: skipped a bad line (" + line + ")");
            }
        }
    }

    /** Reads the "4001:6;4003:2" column back into real objects. */
    private void restorePrescriptions(
            Hospital hospital,
            Patient patient,
            String field) {

        if (field == null || field.isBlank()) {
            return;
        }

        for (String pair : field.split(";")) {

            String[] parts = pair.split(":");

            if (parts.length != 2) {
                warnings.add("Patient " + patient.getId()
                        + " has an unreadable prescription (" + pair + ")");
                continue;
            }

            try {
                int medicineId = Integer.parseInt(parts[0].trim());
                int quantity = Integer.parseInt(parts[1].trim());

                Optional<Medicine> medicine = hospital.findMedicine(medicineId);

                if (medicine.isPresent()) {
                    // The saved stock already has this amount subtracted,
                    // so the chart is restored without touching the stock.
                    patient.restorePrescription(medicine.get(), quantity);

                } else {
                    warnings.add("Patient " + patient.getId()
                            + " referred to medicine " + medicineId
                            + ", which is no longer on file");
                }

            } catch (NumberFormatException e) {
                warnings.add("Patient " + patient.getId()
                        + " has an unreadable prescription (" + pair + ")");
            }
        }
    }

    // ------------------------------------------------------------------
    // small helpers
    // ------------------------------------------------------------------

    /** Returns the data lines of a file, or an empty list if it is new. */
    private List<String> readLines(Path file) {

        List<String> lines = new ArrayList<>();

        if (!Files.exists(file)) {
            return lines;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            String line;

            while ((line = reader.readLine()) != null) {

                String trimmed = line.trim();

                if (!trimmed.isEmpty() && !trimmed.startsWith(COMMENT)) {
                    lines.add(trimmed);
                }
            }

        } catch (IOException e) {
            throw new DataAccessException(
                    "Could not read " + file.getFileName() + ": "
                            + e.getMessage(), e);
        }

        return lines;
    }

    private String[] split(String line, int expected, String what) {

        String[] columns = line.split(SEP, -1);

        if (columns.length != expected) {
            throw new IllegalArgumentException(
                    "A " + what + " record needs " + expected + " columns");
        }

        return columns;
    }
}
