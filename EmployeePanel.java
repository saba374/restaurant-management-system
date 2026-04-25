package com.saba.restaurant.gui;

import com.saba.restaurant.dao.EmployeeDAO;
import com.saba.restaurant.models.Employee;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for managing restaurant employees.
 * Author: Saba
 */
public class EmployeePanel extends JPanel {

    private final MainFrame mainFrame;
    private final EmployeeDAO empDAO = new EmployeeDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfName, tfPhone, tfEmail, tfSalary;
    private JComboBox<Employee.Role> cbRole;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedId = -1;

    private static final Color PRIMARY = new Color(30, 58, 95);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER  = new Color(231, 76, 60);

    public EmployeePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel title = new JLabel("👷 Employee Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        String[] cols = {"ID", "Name", "Role", "Phone", "Email", "Salary (Rs.)", "Hired Date"};
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
        table.setGridColor(new Color(220,220,220));
        table.setSelectionBackground(new Color(52,152,219,60));
        int[] widths = {40,160,100,130,200,120,150};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) populateForm(); });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(200,200,200)));

        JPanel leftPanel = new JPanel(new BorderLayout(0,8));
        leftPanel.setOpaque(false);
        leftPanel.add(title, BorderLayout.NORTH);
        leftPanel.add(scroll, BorderLayout.CENTER);

        JPanel formPanel = buildForm();
        formPanel.setPreferredSize(new Dimension(300, 0));

        add(leftPanel, BorderLayout.CENTER);
        add(formPanel, BorderLayout.EAST);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200,200,200),1,true),
            BorderFactory.createEmptyBorder(20,18,20,18)));

        JLabel title = new JLabel("Employee Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        tfName   = field(panel, "Full Name *");
        JLabel roleLbl = new JLabel("Role *");
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLbl.setForeground(new Color(80,80,80));
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(roleLbl); panel.add(Box.createVerticalStrut(3));
        cbRole = new JComboBox<>(Employee.Role.values());
        cbRole.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cbRole); panel.add(Box.createVerticalStrut(10));

        tfPhone  = field(panel, "Phone Number");
        tfEmail  = field(panel, "Email Address");
        tfSalary = field(panel, "Monthly Salary (Rs.)");

        panel.add(Box.createVerticalStrut(10));
        JPanel btnP = new JPanel(new GridLayout(2,2,8,8));
        btnP.setOpaque(false); btnP.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        btnAdd    = btn("➕ Add",    SUCCESS);
        btnUpdate = btn("✏  Update", new Color(52,152,219));
        btnDelete = btn("🗑  Delete", DANGER);
        btnClear  = btn("🔄 Clear",  new Color(149,165,166));
        btnUpdate.setEnabled(false); btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> addEmp());
        btnUpdate.addActionListener(e -> updateEmp());
        btnDelete.addActionListener(e -> deleteEmp());
        btnClear.addActionListener(e -> clearForm());

        btnP.add(btnAdd); btnP.add(btnUpdate); btnP.add(btnDelete); btnP.add(btnClear);
        panel.add(btnP);
        return panel;
    }

    private JTextField field(JPanel panel, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(80,80,80));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl); panel.add(Box.createVerticalStrut(3));
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200,200,200),1,true),
            BorderFactory.createEmptyBorder(5,8,5,8)));
        panel.add(tf); panel.add(Box.createVerticalStrut(10));
        return tf;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            for (Employee e : empDAO.getAllEmployees())
                tableModel.addRow(new Object[]{e.getId(), e.getName(), e.getRole(), e.getPhone(),
                    e.getEmail(), String.format("%.2f", e.getSalary()), e.getHiredDate()});
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        tfName.setText((String) tableModel.getValueAt(row, 1));
        cbRole.setSelectedItem(tableModel.getValueAt(row, 2));
        tfPhone.setText((String) tableModel.getValueAt(row, 3));
        tfEmail.setText((String) tableModel.getValueAt(row, 4));
        tfSalary.setText(tableModel.getValueAt(row, 5).toString());
        btnUpdate.setEnabled(true); btnDelete.setEnabled(true); btnAdd.setEnabled(false);
    }

    private void addEmp() {
        if (!validate()) return;
        try {
            Employee e = new Employee(tfName.getText().trim(), (Employee.Role) cbRole.getSelectedItem(),
                tfPhone.getText().trim(), tfEmail.getText().trim(), parseSalary());
            empDAO.addEmployee(e);
            JOptionPane.showMessageDialog(this,"Employee added!","Success",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    private void updateEmp() {
        if (selectedId==-1 || !validate()) return;
        try {
            Employee e = new Employee(tfName.getText().trim(), (Employee.Role) cbRole.getSelectedItem(),
                tfPhone.getText().trim(), tfEmail.getText().trim(), parseSalary());
            e.setId(selectedId);
            empDAO.updateEmployee(e);
            JOptionPane.showMessageDialog(this,"Employee updated!","Success",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData();
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    private void deleteEmp() {
        if (selectedId==-1) return;
        if (JOptionPane.showConfirmDialog(this,"Delete this employee?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        try { empDAO.deleteEmployee(selectedId); clearForm(); loadData(); }
        catch (SQLException e) { showErr(e.getMessage()); }
    }

    private boolean validate() {
        if (tfName.getText().trim().isEmpty()) { showErr("Name is required."); return false; }
        try { parseSalary(); } catch (NumberFormatException e) { showErr("Enter a valid salary."); return false; }
        return true;
    }

    private double parseSalary() {
        String s = tfSalary.getText().trim();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }

    private void clearForm() {
        selectedId=-1; tfName.setText(""); tfPhone.setText(""); tfEmail.setText(""); tfSalary.setText("");
        cbRole.setSelectedIndex(0); table.clearSelection();
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
