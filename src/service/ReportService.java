package service;

import exceptions.DataAccessException;
import model.Doctor;
import model.Hospital;
import model.Medicine;
import model.Nurse;
import model.Patient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builds the text report shown on the dashboard and writes it to a file
 * the user chooses. This is the "generate reports" part of the brief.
 */
public class ReportService {

    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final Hospital hospital;

    public ReportService(Hospital hospital) {
        this.hospital = hospital;
    }

    public String buildReport() {

        StringBuilder report = new StringBuilder();

        line(report, "=");
        report.append(hospital.getName().toUpperCase())
              .append(" - SUMMARY REPORT\n");
        report.append("Generated: ")
              .append(LocalDateTime.now().format(STAMP))
              .append("\n");
        line(report, "=");

        report.append("\nOVERVIEW\n");
        report.append(String.format("  Doctors registered : %d%n",
                hospital.countDoctors()));
        report.append(String.format("  Nurses registered  : %d%n",
                hospital.countNurses()));
        report.append(String.format("  Patients registered: %d%n",
                hospital.countPatients()));
        report.append(String.format("  Currently admitted : %d%n",
                hospital.countAdmittedPatients()));
        report.append(String.format("  Medicines in stock : %d%n",
                hospital.countMedicines()));
        report.append(String.format("  Low stock (< 10)   : %d%n",
                hospital.countLowStockMedicines()));
        report.append(String.format("  Pharmacy value     : %.2f BDT%n",
                hospital.pharmacyStockValue()));

        report.append("\nDOCTORS\n");

        if (hospital.getDoctors().isEmpty()) {
            report.append("  (none)\n");
        }

        for (Doctor doctor : hospital.getDoctors()) {
            report.append(String.format("  %-6d %-24s %-22s %s%n",
                    doctor.getId(), doctor.getName(),
                    doctor.getSpecialization(),
                    doctor.getDepartment().getLabel()));
        }

        report.append("\nNURSES\n");

        if (hospital.getNurses().isEmpty()) {
            report.append("  (none)\n");
        }

        for (Nurse nurse : hospital.getNurses()) {
            report.append(String.format("  %-6d %-24s %-10s %s%n",
                    nurse.getId(), nurse.getName(),
                    nurse.getShift().getLabel(),
                    nurse.getQualification()));
        }

        report.append("\nPATIENTS\n");

        if (hospital.getPatients().isEmpty()) {
            report.append("  (none)\n");
        }

        for (Patient patient : hospital.getPatients()) {

            String nurseName = patient.getAssignedNurse() == null
                    ? "no nurse"
                    : patient.getAssignedNurse().getName();

            report.append(String.format("  %-6d %-24s %-18s %-12s %s%n",
                    patient.getId(), patient.getName(),
                    patient.getDisease(),
                    patient.getStatus().getLabel(),
                    nurseName));

            report.append("         medicines: ")
                  .append(patient.medicineSummary())
                  .append("\n");
        }

        report.append("\nPHARMACY\n");

        if (hospital.getMedicines().isEmpty()) {
            report.append("  (none)\n");
        }

        for (Medicine medicine : hospital.getMedicines()) {
            report.append(String.format("  %-6d %-22s %-14s qty %-6d %8.2f%n",
                    medicine.getId(), medicine.getName(),
                    medicine.getManufacturer(),
                    medicine.getQuantity(), medicine.getPrice()));
        }

        report.append("\n");
        line(report, "-");
        report.append("End of report\n");

        return report.toString();
    }

    /** Writes the report to the file the user picked in the save dialog. */
    public void exportReport(File file) {

        try (BufferedWriter writer = Files.newBufferedWriter(
                file.toPath(), StandardCharsets.UTF_8)) {

            writer.write(buildReport());

        } catch (IOException e) {
            throw new DataAccessException(
                    "Could not write the report: " + e.getMessage(), e);
        }
    }

    private void line(StringBuilder builder, String character) {
        builder.append(character.repeat(64)).append("\n");
    }
}
