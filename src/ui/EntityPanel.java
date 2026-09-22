package ui;

import exceptions.HospitalException;
import interfaces.Identifiable;
import interfaces.Searchable;
import service.BaseService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * The shared skeleton of the Doctors, Nurses, Patients and Medicines
 * tabs: a search box, a table, and the Add / Edit / Delete buttons.
 *
 * This is the GUI half of the OOP story:
 *   - it is generic, so one class drives four different entity types
 *   - it is abstract: a subclass must say what its columns are, how a
 *     record becomes a row, and what its dialogs look like
 *   - every button handler runs inside runSafely(), so a
 *     HospitalException thrown deep in the service layer always ends up
 *     in a message box instead of on the console.
 */
public abstract class EntityPanel<T extends Identifiable & Searchable>
        extends JPanel implements Refreshable {

    private final BaseService<T> service;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final JLabel countLabel;

    /** The records currently shown, in table order. */
    private List<T> visible = new ArrayList<>();

    protected EntityPanel(BaseService<T> service, String title, String subtitle) {

        this.service = service;

        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(16, 16, 16, 16));

        // --- table ---------------------------------------------------

        tableModel = new DefaultTableModel(columnNames(), 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // records are changed through the dialog
            }

            @Override
            public Class<?> getColumnClass(int column) {

                // So the ID and Quantity columns sort as numbers, not
                // as text, when the user clicks the header.
                if (getRowCount() == 0) {
                    return Object.class;
                }

                Object value = getValueAt(0, column);

                return value == null ? Object.class : value.getClass();
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UiTheme.styleTable(table);

        // Double click a row to edit it.
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent event) {

                if (event.getClickCount() == 2) {
                    editSelected();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UiTheme.GRID));
        scroll.getViewport().setBackground(UiTheme.CARD);

        // --- top bar -------------------------------------------------

        searchField = new JTextField(18);
        searchField.setFont(UiTheme.BODY_FONT);
        searchField.setToolTipText("Type to filter, then press Enter");

        searchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent event) {
                refresh();
            }
        });

        JButton clear = UiTheme.button("Clear", UiTheme.MUTED);
        clear.addActionListener(event -> {
            searchField.setText("");
            refresh();
        });

        JPanel search = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        search.setOpaque(false);
        search.add(new JLabel("Search:"));
        search.add(searchField);
        search.add(clear);

        JPanel top = UiTheme.header(title, subtitle);
        top.add(search, BorderLayout.EAST);

        // --- buttons -------------------------------------------------

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        JButton add = UiTheme.button("Add", UiTheme.PRIMARY);
        add.addActionListener(event -> runSafely(this::openCreateDialog));

        JButton edit = UiTheme.button("Edit", UiTheme.ACCENT);
        edit.addActionListener(event -> editSelected());

        JButton delete = UiTheme.button("Delete", UiTheme.DANGER);
        delete.addActionListener(event -> deleteSelected());

        buttons.add(add);
        buttons.add(edit);
        buttons.add(delete);

        for (JButton extra : extraButtons()) {
            buttons.add(extra);
        }

        JButton reload = UiTheme.button("Refresh", UiTheme.PRIMARY_DARK);
        reload.addActionListener(event -> refresh());
        buttons.add(reload);

        countLabel = UiTheme.subtitle(" ");

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(UiTheme.padding(12, 0, 0, 0));
        bottom.add(buttons, BorderLayout.WEST);

        JPanel countHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        countHolder.setOpaque(false);
        countHolder.add(countLabel);
        bottom.add(countHolder, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------
    // what every subclass must provide
    // ------------------------------------------------------------------

    protected abstract String[] columnNames();

    protected abstract Object[] toRow(T item);

    protected abstract void openCreateDialog();

    protected abstract void openEditDialog(T item);

    protected abstract void deleteItem(T item);

    /** Text shown in the delete confirmation box. */
    protected abstract String describe(T item);

    /** Panels with more actions (patients, medicines) override this. */
    protected List<JButton> extraButtons() {
        return new ArrayList<>();
    }

    // ------------------------------------------------------------------
    // shared behaviour
    // ------------------------------------------------------------------

    /** READ: reloads the table, honouring whatever is in the search box. */
    @Override
    public void refresh() {

        runSafely(() -> {

            int previouslySelected = selectedId();

            visible = service.search(searchField.getText());

            tableModel.setRowCount(0);

            for (T item : visible) {
                tableModel.addRow(toRow(item));
            }

            countLabel.setText(visible.size() + " record(s) shown");

            reselect(previouslySelected);
        });
    }

    private int selectedId() {

        T selected = getSelected();

        return selected == null ? -1 : selected.getId();
    }

    private void reselect(int id) {

        if (id < 0) {
            return;
        }

        for (int row = 0; row < visible.size(); row++) {

            if (visible.get(row).getId() == id) {

                int viewRow = table.convertRowIndexToView(row);

                if (viewRow >= 0) {
                    table.setRowSelectionInterval(viewRow, viewRow);
                }

                return;
            }
        }
    }

    /** The record on the highlighted row, or null. */
    protected T getSelected() {

        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            return null;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);

        if (modelRow < 0 || modelRow >= visible.size()) {
            return null;
        }

        return visible.get(modelRow);
    }

    /** Same as getSelected(), but nags the user if nothing is picked. */
    protected T requireSelected() {

        T selected = getSelected();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a row in the table first.",
                    "Nothing selected",
                    JOptionPane.WARNING_MESSAGE);
        }

        return selected;
    }

    private void editSelected() {

        T selected = requireSelected();

        if (selected != null) {
            runSafely(() -> openEditDialog(selected));
        }
    }

    private void deleteSelected() {

        T selected = requireSelected();

        if (selected == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Delete " + describe(selected) + "?\n"
                        + "This cannot be undone.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (answer == JOptionPane.YES_OPTION) {

            runSafely(() -> {
                deleteItem(selected);
                refresh();
                info("Deleted " + describe(selected) + ".");
            });
        }
    }

    // ------------------------------------------------------------------
    // error handling used by every button on every panel
    // ------------------------------------------------------------------

    /**
     * Runs a piece of GUI work with one try-catch around it.
     *
     * HospitalException carries its own dialog title, so validation,
     * not-found, out-of-stock and file errors are all reported the same
     * way without a chain of instanceof checks.
     */
    protected void runSafely(Runnable action) {

        try {
            action.run();

        } catch (HospitalException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    e.getTitle(),
                    JOptionPane.ERROR_MESSAGE);

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Something went wrong: " + e,
                    "Unexpected error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void info(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Done",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
