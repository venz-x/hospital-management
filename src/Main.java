import model.Hospital;
import service.DoctorService;
import service.MedicineService;
import service.NurseService;
import service.PatientService;
import service.SampleData;
import storage.DataStore;
import ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point.
 *
 * Reads the data files, fills the hospital with a few sample rows the
 * very first time it runs, then opens the Swing window on the event
 * dispatch thread.
 */
public class Main {

    private static final String HOSPITAL_NAME = "City Care Hospital";

    public static void main(String[] args) {

        // "data" by default, or a folder passed on the command line.
        String folder = args.length > 0 ? args[0] : "data";

        Hospital hospital = new Hospital(HOSPITAL_NAME);
        DataStore store = new DataStore(folder);

        boolean firstRun = store.isEmpty();

        try {
            store.loadAll(hospital);

            if (firstRun) {
                loadSampleData(hospital, store);
            }

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "The saved data could not be read:\n" + e.getMessage()
                            + "\n\nThe program will start with an empty database.",
                    "Startup problem",
                    JOptionPane.ERROR_MESSAGE);

            hospital.clearAll();
        }

        applyLookAndFeel();

        SwingUtilities.invokeLater(() -> {

            MainFrame frame = new MainFrame(hospital, store);

            frame.setVisible(true);
            frame.showWarnings(store.getWarnings());
        });
    }

    private static void loadSampleData(Hospital hospital, DataStore store) {

        SampleData.load(
                new DoctorService(hospital, store),
                new NurseService(hospital, store),
                new PatientService(hospital, store),
                new MedicineService(hospital, store));
    }

    /** Nimbus looks much better than the default Metal theme. */
    private static void applyLookAndFeel() {

        try {
            for (UIManager.LookAndFeelInfo info
                    : UIManager.getInstalledLookAndFeels()) {

                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

        } catch (Exception e) {
            // Not a problem: the default look and feel still works.
            System.out.println("Nimbus not available, using the default theme.");
        }
    }
}
