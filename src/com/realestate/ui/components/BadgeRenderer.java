package com.realestate.ui.components;

import com.realestate.ui.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class BadgeRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(Theme.FONT_BOLD);

        if (value == null) {
            return label;
        }

        String text = value.toString().toUpperCase();
        if (text.contains("APPROVED") || text.contains("ACTIVE")) {
            label.setForeground(new Color(0, 100, 0));
            label.setBackground(new Color(230, 255, 230));
        } else if (text.contains("PENDING")) {
            label.setForeground(new Color(160, 80, 0));
            label.setBackground(new Color(255, 245, 220));
        } else if (text.contains("REJECTED") || text.contains("INACTIVE") || text.contains("TERMINATED")) {
            label.setForeground(new Color(160, 0, 0));
            label.setBackground(new Color(255, 230, 230));
        } else if (text.contains("RENTED")) {
            label.setForeground(new Color(0, 50, 140));
            label.setBackground(new Color(225, 240, 255));
        } else {
            label.setForeground(Color.BLACK);
            label.setBackground(Color.WHITE);
        }

        label.setOpaque(true);
        return label;
    }
}
