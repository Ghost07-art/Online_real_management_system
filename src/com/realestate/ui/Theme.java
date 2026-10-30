package com.realestate.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class Theme {
    public static final Color PRIMARY = new Color(220, 235, 250);
    public static final Color PRIMARY_DARK = new Color(16, 44, 70);

    public static final Color BG_LIGHT = new Color(242, 242, 242);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER = new Color(180, 180, 180);

    public static final Color TEXT_PRIMARY = Color.BLACK;
    public static final Color TEXT_MUTED = new Color(60, 60, 60);

    public static final Color SUCCESS = new Color(220, 245, 225);
    public static final Color SUCCESS_LIGHT = new Color(225, 245, 230);
    public static final Color WARNING = new Color(180, 100, 10);
    public static final Color WARNING_LIGHT = new Color(255, 245, 220);
    public static final Color DANGER = new Color(255, 225, 225);
    public static final Color DANGER_LIGHT = new Color(255, 230, 230);
    public static final Color INFO = new Color(20, 90, 150);
    public static final Color INFO_LIGHT = new Color(225, 240, 255);

    public static final Font FONT_HEADER = new Font("Arial", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Arial", Font.BOLD, 14);
    public static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Arial", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Arial", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 11);

    public static JButton createButton(String text, Color bg, Color border) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setOpaque(true);
        return btn;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, new Color(220, 235, 250), new Color(100, 150, 210));
    }

    public static JButton createSuccessButton(String text) {
        return createButton(text, new Color(220, 245, 225), new Color(90, 175, 105));
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, new Color(255, 225, 225), new Color(210, 100, 100));
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(new Color(235, 235, 235));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.setOpaque(true);
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setForeground(Color.BLACK);
        tf.setBackground(Color.WHITE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_REGULAR);
        pf.setForeground(Color.BLACK);
        pf.setBackground(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        return pf;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(26);
        table.setForeground(Color.BLACK);
        table.setBackground(Color.WHITE);
        table.setShowGrid(true);
        table.setGridColor(new Color(210, 210, 210));
        table.setSelectionBackground(new Color(180, 210, 240));
        table.setSelectionForeground(Color.BLACK);

        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(225, 225, 225));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 28));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(BORDER, 1));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        renderer.setBorder(new EmptyBorder(0, 6, 0, 6));
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}
