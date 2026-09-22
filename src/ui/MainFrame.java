package ui;

import exceptions.HospitalException;
import model.Hospital;
import service.DoctorService;
import service.MedicineService;
import service.NurseService;
import service.PatientService;
import service.ReportService;
import storage.DataStore;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * The main window: a tab for each part of the system.
 *
 * It owns the services and hands them to the panels, so the panels
 * never build anything themselves.
 */
public class MainFrame extends JFrame {

    private final Hospital hospital;
    private final DataStore store;

    private final DoctorService doctorService;
    private final NurseService nurseService;
    private final PatientService patientService;
    private final MedicineService medicineService;
    private final ReportService reportService;

    private final JTabbedPane tabs = new JTabbedPane();

    private DashboardPanel dashboardPanel;
    private DoctorPanel doctorPanel;
    private NursePanel nursePanel;
    private PatientPanel patientPanel;
    private MedicinePanel medicinePanel;

    public MainFrame(Hospital hospital, DataStore store) {

        this.hospital = hospital;
        this.store = store;

        this.doctorService = new DoctorService(hospital, store);
        this.nurseService = new NurseService(hospital, store);
        this.patientService = new PatientService(hospital, store);
        this.medicineService = new MedicineService(hospital, store);
        this.reportService = new ReportService(hospital);

        setTitle(hospital.getName() + " - Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(980, 600));
        setSize(1180, 720);
        setLocationRelativeTo(null);

        buildTabs();
        setJMenuBar(buildMenuBar());

        getContentPane().setBackground(UiTheme.BACKGROUND);
        add(tabs, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent event) {
                confirmExit();
            }
        });
    }

    private void buildTabs() {

        dashboardPanel = new DashboardPanel(
                hospital, reportService, this::reloadFromDisk);

        doctorPanel = new DoctorPanel(doctorService);
        nursePanel = new NursePanel(nurseService);
        patientPanel = new PatientPanel(
                patientService, nurseService, medicineService);
        medicinePanel = new MedicinePanel(medicineService);

        tabs.setFont(UiTheme.BOLD_FONT);

        tabs.addTab("Dashboard", dashboardPanel);
        tabs.addTab("Doctors", doctorPanel);
        tabs.addTab("Nurses", nursePanel);
        tabs.addTab("Patients", patientPanel);
        tabs.addTab("Pharmacy", medicinePanel);

        // A change on one tab (a new nurse, less stock) shows up on the
        // others as soon as they are opened.
        tabs.addChangeListener(event -> {

            java.awt.Component selected = tabs.getSelectedComponent();

            if (selected instanceof Refreshable refreshable) {
                refreshable.refresh();
            }
        });
    }

    private JMenuBar buildMenuBar() {

        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");

        JMenuItem save = new JMenuItem("Save now");
        save.addActionListener(event -> saveNow());

        JMenuItem reload = new JMenuItem("Reload from file");
        reload.addActionListener(event -> reloadFromDisk());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(event -> confirmExit());

        file.add(save);
        file.add(reload);
        file.addSeparator();
        file.add(exit);

        JMenu help = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(event -> showAbout());

        JMenuItem where = new JMenuItem("Where is my data?");
        where.addActionListener(event -> JOptionPane.showMessageDialog(
                this,
                "Records are stored as plain text here:\n"
                        + store.getDataDir().toAbsolutePath(),
                "Data folder",
                JOptionPane.INFORMATION_MESSAGE));

        help.add(where);
        help.add(about);

        bar.add(file);
        bar.add(help);

        return bar;
    }

    // ------------------------------------------------------------------
    // file actions
    // ------------------------------------------------------------------

    private void saveNow() {

        try {
            store.saveAll(hospital);

            JOptionPane.showMessageDialog(
                    this,
                    "All records were written to:\n"
                            + store.getDataDir().toAbsolutePath(),
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (HospitalException e) {

            JOptionPane.showMessageDialog(
                    this, e.getMessage(), e.getTitle(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadFromDisk() {

        try {
            store.loadAll(hospital);

            refreshAll();
            showWarnings(store.getWarnings());

        } catch (HospitalException e) {

            JOptionPane.showMessageDialog(
                    this, e.getMessage(), e.getTitle(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshAll() {

        dashboardPanel.refresh();
        doctorPanel.refresh();
        nursePanel.refresh();
        patientPanel.refresh();
        medicinePanel.refresh();
    }

    /** Shows anything the loader could not read, without stopping. */
    public void showWarnings(List<String> warnings) {

        if (warnings.isEmpty()) {
            return;
        }

        JTextArea area = new JTextArea(String.join("\n", warnings));
        area.setEditable(false);
        area.setFont(UiTheme.MONO_FONT);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 180));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Some records were skipped",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showAbout() {

        JOptionPane.showMessageDialog(
                this,
                hospital.getName() + " Management System\n\n"
                        + "Java Swing front end, plain text file storage.\n"
                        + "Object oriented programming course project.\n\n"
                        + "Tabs: Dashboard, Doctors, Nurses, Patients, Pharmacy.\n"
                        + "Every add, edit and delete is saved immediately.",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmExit() {

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Close the application?",
                "Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            store.saveAll(hospital);

        } catch (HospitalException e) {

            int stillExit = JOptionPane.showConfirmDialog(
                    this,
                    "The data could not be saved:\n" + e.getMessage()
                            + "\n\nExit anyway?",
                    e.getTitle(),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);

            if (stillExit != JOptionPane.YES_OPTION) {
                return;
            }
        }

        dispose();
        System.exit(0);
    }
}
