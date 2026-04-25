package com.saba.restaurant.gui;

import com.saba.restaurant.dao.MenuDAO;
import com.saba.restaurant.models.MenuItem;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for managing menu items (CRUD).
 * Author: Saba
 */
public class MenuPanel extends JPanel {

    private final MainFrame mainFrame;
    private final MenuDAO menuDAO = new MenuDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;

    // Form fields
    private JTextField tfName, tfPrice, tfDescription;
    private JComboBox<String> cbCategory;
    private JCheckBox chkAvailable;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private int selectedId = -1;

    private static final String[] CATEGORIES = {"Starter", "Main Course", "Bread", "Side", "Drinks", "Dessert"};
    private static final Color PRIMARY = new Color(30, 58, 95);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER  = new Color(231, 76, 60);
    private static final Color WARNING = new Color(241, 196, 15);

    public MenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        buildUI();
        loadTableData(null);
    }

    private void buildUI() {
        // ── Left: Table ────────────────────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout(5, 8));
        leftPanel.setOpaque(false);

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        tfSearch = new JTextField();
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180,180,180), 1, true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        tfSearch.putClientProperty("JTextField.placeholderText", "Search by name or category…");
        JButton btnSearch = btn("🔍 Search", PRIMARY);
        btnSearch.addActionListener(e -> loadTableData(tfSearch.getText().trim()));
        JButton btnShowAll = btn("Show All", SUCCESS);
        btnShowAll.addActionListener(e -> { tfSearch.setText(""); loadTableData(null); });
        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBtns.setOpaque(false);
        searchBtns.add(btnSearch); searchBtns.add(btnShowAll);
        searchPanel.add(new JLabel("🍽  Menu Items"), BorderLayout.WEST);
        searchPanel.add(tfSearch, BorderLayout.CENTER);
        searchPanel.add(searchBtns, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Name", "Category", "Price (Rs.)", "Description", "Available"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(52, 152, 219, 60));

        // Column widths
        int[] widths = {40, 180, 110, 100, 220, 80};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200)));

        leftPanel.add(searchPanel, BorderLayout.NORTH);
        leftPanel.add(scroll, BorderLayout.CENTER);

        // ── Right: Form ────────────────────────────────────────────────
        JPanel formPanel = buildForm();
        formPanel.setPreferredSize(new Dimension(310, 0));

        add(leftPanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(20, 18, 20, 18)));

        JLabel title = new JLabel("Menu Item Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        tfName = addFormField(panel, "Item Name *");
        cbCategory = new JComboBox<>(CATEGORIES);
        cbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addLabeledComponent(panel, "Category *", cbCategory);
        tfPrice = addFormField(panel, "Price (Rs.) *");
        tfDescription = addFormField(panel, "Description");

        chkAvailable = new JCheckBox("Available for Order");
        chkAvailable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkAvailable.setSelected(true);
        chkAvailable.setOpaque(false);
        chkAvailable.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(8));
        panel.add(chkAvailable);
        panel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        btnAdd    = btn("➕ Add",    SUCCESS);
        btnUpdate = btn("✏  Update", new Color(52, 152, 219));
        btnDelete = btn("🗑  Delete", DANGER);
        btnClear  = btn("🔄 Clear",  new Color(149, 165, 166));

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> addItem());
        btnUpdate.addActionListener(e -> updateItem());
        btnDelete.addActionListener(e -> deleteItem());
        btnClear.addActionListener(e -> clearForm());

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete); btnPanel.add(btnClear);
        panel.add(btnPanel);

        return panel;
    }

    private JTextField addFormField(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(3));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200,200,200), 1, true),
            BorderFactory.createEmptyBorder(5,8,5,8)));
        panel.add(tf);
        panel.add(Box.createVerticalStrut(10));
        return tf;
    }

    private void addLabeledComponent(JPanel panel, String label, JComponent comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(3));
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(comp);
        panel.add(Box.createVerticalStrut(10));
    }

    private void loadTableData(String search) {
        tableModel.setRowCount(0);
        try {
            List<MenuItem> items = menuDAO.getAllMenuItems();
            for (MenuItem m : items) {
                if (search != null && !search.isEmpty()) {
                    if (!m.getName().toLowerCase().contains(search.toLowerCase()) &&
                        !m.getCategory().toLowerCase().contains(search.toLowerCase())) continue;
                }
                tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getCategory(),
                    String.format("%.2f", m.getPrice()), m.getDescription(),
                    m.isAvailable() ? "✅ Yes" : "❌ No"
                });
            }
        } catch (SQLException e) {
            showError("Failed to load menu: " + e.getMessage());
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        tfName.setText((String) tableModel.getValueAt(row, 1));
        cbCategory.setSelectedItem(tableModel.getValueAt(row, 2));
        tfPrice.setText(tableModel.getValueAt(row, 3).toString());
        tfDescription.setText((String) tableModel.getValueAt(row, 4));
        chkAvailable.setSelected(tableModel.getValueAt(row, 5).equals("✅ Yes"));
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
        btnAdd.setEnabled(false);
    }

    private void addItem() {
        if (!validateForm()) return;
        try {
            MenuItem m = buildFromForm();
            menuDAO.addMenuItem(m);
            JOptionPane.showMessageDialog(this, "Menu item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadTableData(null);
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void updateItem() {
        if (selectedId == -1 || !validateForm()) return;
        try {
            MenuItem m = buildFromForm();
            m.setId(selectedId);
            menuDAO.updateMenuItem(m);
            JOptionPane.showMessageDialog(this, "Menu item updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadTableData(null);
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void deleteItem() {
        if (selectedId == -1) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this menu item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            menuDAO.deleteMenuItem(selectedId);
            clearForm();
            loadTableData(null);
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private MenuItem buildFromForm() {
        return new MenuItem(
            tfName.getText().trim(),
            (String) cbCategory.getSelectedItem(),
            Double.parseDouble(tfPrice.getText().trim()),
            tfDescription.getText().trim(),
            chkAvailable.isSelected()
        );
    }

    private boolean validateForm() {
        if (tfName.getText().trim().isEmpty()) { showError("Name is required."); return false; }
        try { Double.parseDouble(tfPrice.getText().trim()); }
        catch (NumberFormatException e) { showError("Enter a valid price."); return false; }
        return true;
    }

    private void clearForm() {
        selectedId = -1;
        tfName.setText(""); tfPrice.setText(""); tfDescription.setText("");
        cbCategory.setSelectedIndex(0); chkAvailable.setSelected(true);
        table.clearSelection();
        btnAdd.setEnabled(true); btnUpdate.setEnabled(false); btnDelete.setEnabled(false);
    }

    public void refresh() { loadTableData(null); }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
