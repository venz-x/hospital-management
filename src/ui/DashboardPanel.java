package ui;

import exceptions.HospitalException;
import model.Hospital;
import service.ReportService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;

/**
 * The first tab: live counters and a printable summary report.
 */
public class DashboardPanel extends JPanel implements Refreshable {

    private final Hospital hospital;
    private final ReportService reportService;
    private final Runnable reloadAction;

    private final JPanel cards = new JPanel(new GridLayout(2, 4, 12, 12));
    private final JTextArea reportArea = new JTextArea();

    public DashboardPanel(
            Hospital hospital,
            ReportService reportService,
            Runnable reloadAction) {

        this.hospital = hospital;
        this.reportService = reportService;
        this.reloadAction = reloadAction;

        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(16, 16, 16, 16));

        cards.setOpaque(false);

        reportArea.setEditable(false);
        reportArea.setFont(UiTheme.MONO_FONT);
        reportArea.setBackground(UiTheme.CARD);
        reportArea.setBorder(UiTheme.padding(12, 12, 12, 12));

        JScrollPane reportScroll = new JScrollPane(reportArea);
        reportScroll.setBorder(BorderFactory.createLineBorder(UiTheme.GRID));

        JPanel centre = new JPanel(new BorderLayout(0, 14));
        centre.setOpaque(false);
        centre.add(cards, BorderLayout.NORTH);
        centre.add(reportScroll, BorderLayout.CENTER);

        JButton refresh = UiTheme.button("Refresh", UiTheme.PRIMARY);
        refresh.addActionListener(event -> refresh());

        JButton export = UiTheme.button("Export report...", UiTheme.ACCENT);
        export.addActionListener(event -> exportReport());

        JButton reload = UiTheme.button("Reload from file", UiTheme.PRIMARY_DARK);
        reload.addActionListener(event -> reloadFromFile());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.setBorder(UiTheme.padding(12, 0, 0, 0));
        buttons.add(refresh);
        buttons.add(export);
        buttons.add(reload);

        add(UiTheme.header(
                hospital.getName(),
                "Everything is saved to text files as soon as you change it"),
                BorderLayout.NORTH);

        add(centre, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    @Override
    public void refresh() {

        cards.removeAll();

        cards.add(statCard("Doctors",
                String.valueOf(hospital.countDoctors()), UiTheme.PRIMARY));
        cards.add(statCard("Nurses",
                String.valueOf(hospital.countNurses()), UiTheme.PRIMARY));
        cards.add(statCard("Patients",
                String.valueOf(hospital.countPatients()), UiTheme.PRIMARY));
        cards.add(statCard("Medicines",
                String.valueOf(hospital.countMedicines()), UiTheme.PRIMARY));

        cards.add(statCard("Admitted now",
                String.valueOf(hospital.countAdmittedPatients()), UiTheme.ACCENT));
        cards.add(statCard("Low stock items",
                String.valueOf(hospital.countLowStockMedicines()), UiTheme.DANGER));
        cards.add(statCard("Pharmacy value",
                String.format("%.2f", hospital.pharmacyStockValue()), UiTheme.ACCENT));
        cards.add(statCard("Total people",
                String.valueOf(hospital.countDoctors()
                        + hospital.countNurses()
                        + hospital.countPatients()), UiTheme.PRIMARY));

        cards.revalidate();
        cards.repaint();

        try {
            reportArea.setText(reportService.buildReport());
            reportArea.setCaretPosition(0);

        } catch (RuntimeException e) {
            reportArea.setText("The report could not be built: " + e.getMessage());
        }
    }

    private JPanel statCard(String caption, String value, Color colour) {

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(UiTheme.BODY_FONT);
        captionLabel.setForeground(UiTheme.MUTED);
        captionLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(colour);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(captionLabel);
        inner.add(valueLabel);

        JPanel card = UiTheme.card();
        card.add(inner, BorderLayout.CENTER);

        return card;
    }

    /** Writes the report to a .txt file the user picks. */
    private void exportReport() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save report");
        chooser.setSelectedFile(new File("hospital_report.txt"));
        chooser.setFileFilter(
                new FileNameExtensionFilter("Text file (*.txt)", "txt"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getAbsolutePath() + ".txt");
        }

        try {
            reportService.exportReport(file);

            JOptionPane.showMessageDialog(
                    this,
                    "Report saved to:\n" + file.getAbsolutePath(),
                    "Report saved",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (HospitalException e) {

            JOptionPane.showMessageDialog(
                    this, e.getMessage(), e.getTitle(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Throws away the in-memory copy and reads the files again. */
    private void reloadFromFile() {

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Read the data files again?\n"
                        + "Anything not yet saved will be lost.",
                "Reload data",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (answer == JOptionPane.YES_OPTION) {
            reloadAction.run();
        }
    }
}
