package ui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * A small builder for the add / edit dialogs.
 *
 * Instead of writing the same GridBagLayout code in four panels, each
 * panel calls addText(...) / addCombo(...) and then showDialog(...).
 */
public class FormPanel extends JPanel {

    private final GridBagConstraints constraints = new GridBagConstraints();
    private int row;

    public FormPanel() {

        setLayout(new GridBagLayout());
        setBorder(UiTheme.padding(4, 4, 4, 4));

        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
    }

    public JTextField addText(String label, String initial) {

        JTextField field = new JTextField(initial == null ? "" : initial, 22);
        field.setFont(UiTheme.BODY_FONT);

        addRow(label, field);

        return field;
    }

    public <E> JComboBox<E> addCombo(String label, E[] values, E selected) {

        JComboBox<E> combo = new JComboBox<>(values);
        combo.setFont(UiTheme.BODY_FONT);

        if (selected != null) {
            combo.setSelectedItem(selected);
        }

        addRow(label, combo);

        return combo;
    }

    /**
     * A combo that may hold nothing, used for "no nurse assigned" and
     * for the medicine pickers.
     */
    public <E> JComboBox<E> addNullableCombo(
            String label,
            List<E> values,
            E selected,
            String nullText) {

        DefaultComboBoxModel<E> model = new DefaultComboBoxModel<>();
        model.addElement(null);

        for (E value : values) {
            model.addElement(value);
        }

        JComboBox<E> combo = new JComboBox<>(model);
        combo.setFont(UiTheme.BODY_FONT);
        combo.setSelectedItem(selected);

        combo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                return super.getListCellRendererComponent(
                        list,
                        value == null ? nullText : value,
                        index,
                        isSelected,
                        cellHasFocus);
            }
        });

        addRow(label, combo);

        return combo;
    }

    /** A read-only line, e.g. the id of the record being edited. */
    public void addReadOnly(String label, String value) {

        JLabel text = new JLabel(value);
        text.setFont(UiTheme.BOLD_FONT);

        addRow(label, text);
    }

    private void addRow(String label, JComponent field) {

        JLabel caption = new JLabel(label);
        caption.setFont(UiTheme.BODY_FONT);
        caption.setForeground(UiTheme.MUTED);

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.weightx = 0;
        add(caption, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        add(field, constraints);

        row++;
    }

    /** Shows the form inside an OK / Cancel dialog. */
    public boolean showDialog(Component parent, String title) {

        int answer = JOptionPane.showConfirmDialog(
                parent,
                this,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        return answer == JOptionPane.OK_OPTION;
    }
}
