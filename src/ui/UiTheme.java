package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * One place for the colours, fonts and small widgets used by every
 * panel, so the whole application looks the same.
 */
public final class UiTheme {

    public static final Color PRIMARY = new Color(0x0F766E);
    public static final Color PRIMARY_DARK = new Color(0x0B5A54);
    public static final Color DANGER = new Color(0xB91C1C);
    public static final Color ACCENT = new Color(0x1D4ED8);
    public static final Color BACKGROUND = new Color(0xF1F5F9);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x0F172A);
    public static final Color MUTED = new Color(0x64748B);
    public static final Color GRID = new Color(0xE2E8F0);

    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
    public static final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BOLD_FONT = new Font("SansSerif", Font.BOLD, 13);
    public static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 12);

    private UiTheme() {
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    /**
     * A flat coloured button.
     *
     * BasicButtonUI is installed on purpose: Nimbus paints its own
     * gradient and would ignore setBackground(), so the buttons would
     * all come out grey.
     */
    public static JButton button(String text, Color background) {

        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());
        button.setFont(BOLD_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final Color normal = background;
        final Color hover = background.darker();

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(normal);
            }
        });

        return button;
    }

    public static JLabel title(String text) {

        JLabel label = new JLabel(text);

        label.setFont(TITLE_FONT);
        label.setForeground(TEXT);

        return label;
    }

    public static JLabel subtitle(String text) {

        JLabel label = new JLabel(text);

        label.setFont(SUBTITLE_FONT);
        label.setForeground(MUTED);

        return label;
    }

    /** The white "header" strip at the top of every tab. */
    public static JPanel header(String titleText, String subtitleText) {

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));

        JLabel heading = title(titleText);
        JLabel sub = subtitle(subtitleText);

        heading.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        sub.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        text.add(heading);
        text.add(javax.swing.Box.createVerticalStrut(2));
        text.add(sub);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(padding(0, 0, 12, 0));
        panel.add(text, BorderLayout.WEST);

        return panel;
    }

    /** Shared look for every JTable in the application. */
    public static void styleTable(JTable table) {

        table.setFont(BODY_FONT);
        table.setRowHeight(26);
        table.setGridColor(GRID);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(0xCCFBF1));
        table.setSelectionForeground(TEXT);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(BOLD_FONT);
        table.getTableHeader().setBackground(new Color(0xE2E8F0));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setReorderingAllowed(false);
    }

    /** A rounded white box used by the dashboard counters. */
    public static JPanel card() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRID),
                padding(14, 16, 14, 16)));

        return panel;
    }
}
