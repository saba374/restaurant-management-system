package com.saba.restaurant.gui;

import com.saba.restaurant.dao.TableDAO;
import com.saba.restaurant.models.RestaurantTable;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for managing restaurant tables.
 * Author: Saba
 */
public class TablePanel extends JPanel {

    private final MainFrame mainFrame;
    private final TableDAO tableDAO = new TableDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField tfTableNumber, tfCapacity;
    private JComboBox<RestaurantTable.TableStatus> cbStatus;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private int selectedId = -1;

    private static final Color PRIMARY = new Color(30, 58, 95);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER  = new Color(231, 76, 60);

    public TablePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Table list
        String[] cols = {"ID", "Table No.", "Capacity", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(220,220,220));
        table.setSelectionBackground(new Color(52,152,219,60));

        // Color-coded status renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                String s = val == null ? "" : val.toString();
                setHorizontalAlignment(CENTER);
                if (s.contains("AVAILABLE")) setForeground(new Color(39, 174, 96));
                else if (s.contains("OCCUPIED")) setForeground(new Color(231, 76, 60));
                else if (s.contains("RESERVED")) setForeground(new Color(241, 196, 15));
                else setForeground(new Color(100, 100, 100));
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(200,200,200)));

        // Form
        JPanel formPanel = buildForm();
        formPanel.setPreferredSize(new Dimension(290, 0));

        add(scroll, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200,200,200), 1, true),
            BorderFactory.createEmptyBorder(20, 18, 20, 18)));

        JLabel title = new JLabel("Table Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        tfTableNumber = addField(panel, "Table Number *");
        tfCapacity    = addField(panel, "Seating Capacity *");

        JLabel lbl = new JLabel("Status");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80,80,80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(3));
        cbStatus = new JComboBox<>(RestaurantTable.TableStatus.values());
        cbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cbStatus);
        panel.add(Box.createVerticalStrut(20));

        JPanel btnP = new JPanel(new GridLayout(2, 2, 8, 8));
        btnP.setOpaque(false); btnP.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        btnAdd    = btn("➕ Add",    SUCCESS);
        btnUpdate = btn("✏  Update", new Color(52,152,219));
        btnDelete = btn("🗑  Delete", DANGER);
        btnClear  = btn("🔄 Clear",  new Color(149,165,166));
        btnUpdate.setEnabled(false); btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> addTable());
        btnUpdate.addActionListener(e -> updateTable());
        btnDelete.addActionListener(e -> deleteTable());
        btnClear.addActionListener(e -> clearForm());

        btnP.add(btnAdd); btnP.add(btnUpdate);
        btnP.add(btnDelete); btnP.add(btnClear);
        panel.add(btnP);
        return panel;
    }

    private JTextField addField(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80,80,80));
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

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (RestaurantTable t : tableDAO.getAllTables()) {
                tableModel.addRow(new Object[]{
                    t.getId(), "Table " + t.getTableNumber(), t.getCapacity() + " seats",
                    t.getStatus().name()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading tables: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        String tNum = tableModel.getValueAt(row, 1).toString().replace("Table ", "");
        String cap  = tableModel.getValueAt(row, 2).toString().replace(" seats", "");
        tfTableNumber.setText(tNum);
        tfCapacity.setText(cap);
        cbStatus.setSelectedItem(RestaurantTable.TableStatus.valueOf(tableModel.getValueAt(row, 3).toString()));
        btnUpdate.setEnabled(true); btnDelete.setEnabled(true); btnAdd.setEnabled(false);
    }

    private void addTable() {
        if (!validate()) return;
        try {
            RestaurantTable t = new RestaurantTable(
                Integer.parseInt(tfTableNumber.getText().trim()),
                Integer.parseInt(tfCapacity.getText().trim())
            );
            t.setStatus((RestaurantTable.TableStatus) cbStatus.getSelectedItem());
            tableDAO.addTable(t);
            JOptionPane.showMessageDialog(this, "Table added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void updateTable() {
        if (selectedId == -1 || !validate()) return;
        try {
            RestaurantTable t = new RestaurantTable(
                Integer.parseInt(tfTableNumber.getText().trim()),
                Integer.parseInt(tfCapacity.getText().trim())
            );
            t.setId(selectedId);
            t.setStatus((RestaurantTable.TableStatus) cbStatus.getSelectedItem());
            tableDAO.updateTable(t);
            JOptionPane.showMessageDialog(this, "Table updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void deleteTable() {
        if (selectedId == -1) return;
        if (JOptionPane.showConfirmDialog(this, "Delete this table?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { tableDAO.deleteTable(selectedId); clearForm(); loadData(); }
        catch (SQLException e) { showErr(e.getMessage()); }
    }

    private boolean validate() {
        try { Integer.parseInt(tfTableNumber.getText().trim()); Integer.parseInt(tfCapacity.getText().trim()); return true; }
        catch (NumberFormatException e) { showErr("Enter valid numbers for table number and capacity."); return false; }
    }

    private void clearForm() {
        selectedId = -1;
        tfTableNumber.setText(""); tfCapacity.setText("");
        cbStatus.setSelectedIndex(0); table.clearSelection();
        btnAdd.setEnabled(true); btnUpdate.setEnabled(false); btnDelete.setEnabled(false);
    }

    public void refresh() { loadData(); }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void showErr(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}
