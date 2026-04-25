package com.saba.restaurant.gui;

import com.saba.restaurant.dao.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

/**
 * Dashboard showing key restaurant statistics.
 * Author: Saba
 */
public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final OrderDAO orderDAO     = new OrderDAO();
    private final CustomerDAO custDAO   = new CustomerDAO();
    private final EmployeeDAO empDAO    = new EmployeeDAO();
    private final MenuDAO menuDAO       = new MenuDAO();
    private final TableDAO tableDAO     = new TableDAO();

    // Stat labels
    private JLabel lblTodayOrders, lblTodayRevenue, lblTotalRevenue;
    private JLabel lblCustomers, lblEmployees, lblMenuItems, lblAvailTables, lblActiveOrders;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        buildUI();
    }

    private void buildUI() {
        // Title
        JLabel title = new JLabel("📊  Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(30, 58, 95));
        add(title, BorderLayout.NORTH);

        // Stats grid
        JPanel grid = new JPanel(new GridLayout(2, 4, 18, 18));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        lblTodayOrders  = addStatCard(grid, "📦 Today's Orders",  "0",  new Color(52, 152, 219));
        lblTodayRevenue = addStatCard(grid, "💰 Today's Revenue", "Rs.0", new Color(39, 174, 96));
        lblTotalRevenue = addStatCard(grid, "💎 Total Revenue",   "Rs.0", new Color(155, 89, 182));
        lblActiveOrders = addStatCard(grid, "🔥 Active Orders",   "0",  new Color(231, 76, 60));
        lblCustomers    = addStatCard(grid, "👥 Customers",        "0",  new Color(26, 188, 156));
        lblEmployees    = addStatCard(grid, "👷 Employees",        "0",  new Color(241, 196, 15));
        lblMenuItems    = addStatCard(grid, "🍽  Menu Items",      "0",  new Color(230, 126, 34));
        lblAvailTables  = addStatCard(grid, "🪑 Available Tables", "0",  new Color(52, 73, 94));

        add(grid, BorderLayout.CENTER);

        // Quick actions panel
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), " Quick Actions ",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));

        JButton btnNewOrder = styledButton("🛒 New Order", new Color(52, 152, 219));
        btnNewOrder.addActionListener(e -> mainFrame.switchToOrders());
        actions.add(btnNewOrder);

        JButton btnRefresh = styledButton("🔄 Refresh", new Color(39, 174, 96));
        btnRefresh.addActionListener(e -> refresh());
        actions.add(btnRefresh);

        add(actions, BorderLayout.SOUTH);

        refresh();
    }

    /** Adds a stat card to the grid and returns the value label for later updates. */
    private JLabel addStatCard(JPanel parent, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accent.darker(), 1, true),
            BorderFactory.createEmptyBorder(15, 18, 15, 18)
        ));

        // Accent strip at top
        JPanel strip = new JPanel();
        strip.setBackground(accent);
        strip.setPreferredSize(new Dimension(0, 5));
        card.add(strip, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(100, 100, 100));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);

        card.add(titleLabel, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);

        parent.add(card);
        return valueLabel;
    }

    private JButton styledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void refresh() {
        try {
            lblTodayOrders.setText(String.valueOf(orderDAO.countTodayOrders()));
            lblTodayRevenue.setText("Rs." + String.format("%.0f", orderDAO.todayRevenue()));
            lblTotalRevenue.setText("Rs." + String.format("%.0f", orderDAO.totalRevenue()));
            lblActiveOrders.setText(String.valueOf(orderDAO.getActiveOrders().size()));
            lblCustomers.setText(String.valueOf(custDAO.countCustomers()));
            lblEmployees.setText(String.valueOf(empDAO.countEmployees()));
            lblMenuItems.setText(String.valueOf(menuDAO.countMenuItems()));
            lblAvailTables.setText(String.valueOf(tableDAO.countAvailableTables()));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading dashboard: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
