package com.saba.restaurant;

import com.saba.restaurant.gui.MainFrame;

import javax.swing.*;

/**
 * ╔══════════════════════════════════════════════════════════╗
 * ║       Restaurant Management System                       ║
 * ║       Developed by: Saba                                 ║
 * ║       Language: Java (OOP) + SQLite                      ║
 * ║       GUI: Java Swing                                    ║
 * ╚══════════════════════════════════════════════════════════╝
 *
 * Features:
 *  - Dashboard with live statistics
 *  - Menu Management (CRUD)
 *  - Order Management with cart & bill generation
 *  - Table Management with status tracking
 *  - Customer Management
 *  - Employee Management
 */
public class Main {

    public static void main(String[] args) {
        // Apply Nimbus Look & Feel for modern appearance
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Fall back to system default
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        // Launch on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
