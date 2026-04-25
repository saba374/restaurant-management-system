package com.saba.restaurant.gui;

import com.saba.restaurant.utils.DatabaseInitializer;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window for the Restaurant Management System.
 * Author: Saba
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private MenuPanel menuPanel;
    private OrderPanel orderPanel;
    private TablePanel tablePanel;
    private CustomerPanel customerPanel;
    private EmployeePanel employeePanel;

    public MainFrame() {
        DatabaseInitializer.initialize();
        initUI();
    }

    private void initUI() {
        setTitle("🍽  Restaurant Management System  |  Developed by Saba");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        // Header bar
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        // Tabbed pane
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        dashboardPanel  = new DashboardPanel(this);
        menuPanel       = new MenuPanel(this);
        orderPanel      = new OrderPanel(this);
        tablePanel      = new TablePanel(this);
        customerPanel   = new CustomerPanel(this);
        employeePanel   = new EmployeePanel(this);

        tabbedPane.addTab("  📊 Dashboard  ",   dashboardPanel);
        tabbedPane.addTab("  📋 Menu       ",   menuPanel);
        tabbedPane.addTab("  🛒 Orders     ",   orderPanel);
        tabbedPane.addTab("  🪑 Tables     ",   tablePanel);
        tabbedPane.addTab("  👥 Customers  ",   customerPanel);
        tabbedPane.addTab("  👷 Employees  ",   employeePanel);

        // Refresh dashboard when switching to it
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.refresh();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 58, 95));
        panel.setPreferredSize(new Dimension(0, 65));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        JLabel title = new JLabel("🍽  RESTAURANT MANAGEMENT SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Developed by Saba  |  Powered by Java & SQLite");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(180, 210, 255));
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(title, BorderLayout.WEST);
        panel.add(subtitle, BorderLayout.EAST);
        return panel;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));
        panel.setBackground(new Color(45, 45, 45));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JLabel status = new JLabel("✅  System Ready  |  Database: restaurant.db  |  Author: Saba");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        status.setForeground(new Color(180, 220, 180));
        panel.add(status);
        return panel;
    }

    /** Called from child panels to refresh other panels after data changes. */
    public void refreshAll() {
        SwingUtilities.invokeLater(() -> {
            dashboardPanel.refresh();
            menuPanel.refresh();
            tablePanel.refresh();
            customerPanel.refresh();
            employeePanel.refresh();
            orderPanel.refresh();
        });
    }

    public void switchToOrders() {
        tabbedPane.setSelectedComponent(orderPanel);
        orderPanel.refresh();
    }
}
