package com.saba.restaurant.gui;

import com.saba.restaurant.dao.CustomerDAO;
import com.saba.restaurant.models.Customer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for managing restaurant customers.
 * Author: Saba
 */
public class CustomerPanel extends JPanel {

    private final MainFrame mainFrame;
    private final CustomerDAO custDAO = new CustomerDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;
    private JTextField tfName, tfPhone, tfEmail, tfAddress;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedId = -1;

    private static final Color PRIMARY = new Color(30, 58, 95);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER  = new Color(231, 76, 60);

    public CustomerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        buildUI();
        loadData(null);
    }

    private void buildUI() {
        // Search
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tfSearch = new JTextField();
        tfSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfSearch.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180,180,180),1,true),
            BorderFactory.createEmptyBorder(6,10,6,10)));
        JButton btnS = btn("🔍 Search", PRIMARY);
        JButton btnA = btn("Show All", SUCCESS);
        btnS.addActionListener(e -> loadData(tfSearch.getText().trim()));
        btnA.addActionListener(e -> { tfSearch.setText(""); loadData(null); });
        JPanel sbBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sbBtns.setOpaque(false); sbBtns.add(btnS); sbBtns.add(btnA);
        JLabel lbl = new JLabel("👥 Customers");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(PRIMARY);
        searchBar.add(lbl, BorderLayout.WEST);
        searchBar.add(tfSearch, BorderLayout.CENTER);
        searchBar.add(sbBtns, BorderLayout.EAST);

        // Table
        String[] cols = {"ID", "Name", "Phone", "Email", "Address", "Joined"};
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
        int[] widths = {40,160,120,200,200,130};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) populateForm(); });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(200,200,200)));

        JPanel leftPanel = new JPanel(new BorderLayout(5,5));
        leftPanel.setOpaque(false);
        leftPanel.add(searchBar, BorderLayout.NORTH);
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

        JLabel title = new JLabel("Customer Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        tfName    = field(panel, "Full Name *");
        tfPhone   = field(panel, "Phone Number");
        tfEmail   = field(panel, "Email Address");
        tfAddress = field(panel, "Address");

        panel.add(Box.createVerticalStrut(10));
        JPanel btnP = new JPanel(new GridLayout(2,2,8,8));
        btnP.setOpaque(false); btnP.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        btnAdd    = btn("➕ Add",    SUCCESS);
        btnUpdate = btn("✏  Update", new Color(52,152,219));
        btnDelete = btn("🗑  Delete", DANGER);
        btnClear  = btn("🔄 Clear",  new Color(149,165,166));
        btnUpdate.setEnabled(false); btnDelete.setEnabled(false);

        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
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

    private void loadData(String search) {
        tableModel.setRowCount(0);
        try {
            List<Customer> list = (search != null && !search.isEmpty())
                ? custDAO.searchCustomers(search) : custDAO.getAllCustomers();
            for (Customer c : list)
                tableModel.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress(), c.getCreatedAt()});
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void populateForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        tfName.setText((String) tableModel.getValueAt(row, 1));
        tfPhone.setText((String) tableModel.getValueAt(row, 2));
        tfEmail.setText((String) tableModel.getValueAt(row, 3));
        tfAddress.setText((String) tableModel.getValueAt(row, 4));
        btnUpdate.setEnabled(true); btnDelete.setEnabled(true); btnAdd.setEnabled(false);
    }

    private void addCustomer() {
        if (tfName.getText().trim().isEmpty()) { showErr("Name is required."); return; }
        try {
            Customer c = new Customer(tfName.getText().trim(), tfPhone.getText().trim(),
                tfEmail.getText().trim(), tfAddress.getText().trim());
            custDAO.addCustomer(c);
            JOptionPane.showMessageDialog(this,"Customer added!","Success",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData(null);
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void updateCustomer() {
        if (selectedId == -1) return;
        try {
            Customer c = new Customer(tfName.getText().trim(), tfPhone.getText().trim(),
                tfEmail.getText().trim(), tfAddress.getText().trim());
            c.setId(selectedId);
            custDAO.updateCustomer(c);
            JOptionPane.showMessageDialog(this,"Customer updated!","Success",JOptionPane.INFORMATION_MESSAGE);
            clearForm(); loadData(null);
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void deleteCustomer() {
        if (selectedId == -1) return;
        if (JOptionPane.showConfirmDialog(this,"Delete this customer?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        try { custDAO.deleteCustomer(selectedId); clearForm(); loadData(null); }
        catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void clearForm() {
        selectedId = -1;
        tfName.setText(""); tfPhone.setText(""); tfEmail.setText(""); tfAddress.setText("");
        table.clearSelection();
        btnAdd.setEnabled(true); btnUpdate.setEnabled(false); btnDelete.setEnabled(false);
    }

    public void refresh() { loadData(null); }

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
