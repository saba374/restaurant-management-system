package com.saba.restaurant.gui;

import com.saba.restaurant.dao.*;
import com.saba.restaurant.models.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for creating and managing orders with bill generation.
 * Author: Saba
 */
public class OrderPanel extends JPanel {

    private final MainFrame mainFrame;
    private final OrderDAO orderDAO   = new OrderDAO();
    private final MenuDAO menuDAO     = new MenuDAO();
    private final CustomerDAO custDAO = new CustomerDAO();
    private final EmployeeDAO empDAO  = new EmployeeDAO();
    private final TableDAO tableDAO   = new TableDAO();

    // New Order section
    private JComboBox<Customer> cbCustomer;
    private JComboBox<RestaurantTable> cbTable;
    private JComboBox<Employee> cbWaiter;
    private JComboBox<MenuItem> cbMenuItem;
    private JSpinner spnQty;
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblTotal;
    private List<OrderItem> cart = new ArrayList<>();

    // Active orders section
    private DefaultTableModel ordersModel;
    private JTable ordersTable;
    private JComboBox<Order.Status> cbStatus;

    private static final Color PRIMARY = new Color(30, 58, 95);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER  = new Color(231, 76, 60);

    public OrderPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(12, 0));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        buildUI();
        loadCombos();
        loadActiveOrders();
    }

    private void buildUI() {
        // Left: New Order
        JPanel newOrderPanel = buildNewOrderPanel();
        newOrderPanel.setPreferredSize(new Dimension(530, 0));

        // Right: Active Orders
        JPanel activePanel = buildActiveOrdersPanel();

        add(newOrderPanel, BorderLayout.WEST);
        add(activePanel, BorderLayout.CENTER);
    }

    // ── New Order Panel ────────────────────────────────────────────────
    private JPanel buildNewOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200,200,200),1,true),
            BorderFactory.createEmptyBorder(18,18,18,18)));

        JLabel title = new JLabel("🛒 New Order");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        // Order info (customer, table, waiter)
        JPanel infoPanel = new JPanel(new GridLayout(3,2,8,8));
        infoPanel.setOpaque(false);
        cbCustomer = new JComboBox<>(); cbCustomer.setFont(new Font("Segoe UI",Font.PLAIN,12));
        cbTable    = new JComboBox<>(); cbTable.setFont(new Font("Segoe UI",Font.PLAIN,12));
        cbWaiter   = new JComboBox<>(); cbWaiter.setFont(new Font("Segoe UI",Font.PLAIN,12));
        infoPanel.add(label("Customer:")); infoPanel.add(cbCustomer);
        infoPanel.add(label("Table:"));   infoPanel.add(cbTable);
        infoPanel.add(label("Waiter:"));  infoPanel.add(cbWaiter);

        // Item adder
        JPanel itemAdder = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        itemAdder.setOpaque(false);
        itemAdder.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200,200,200)),
            " Add Item ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI",Font.BOLD,12)));

        cbMenuItem = new JComboBox<>();
        cbMenuItem.setFont(new Font("Segoe UI",Font.PLAIN,12));
        cbMenuItem.setPreferredSize(new Dimension(210, 30));

        spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spnQty.setFont(new Font("Segoe UI",Font.PLAIN,12));
        spnQty.setPreferredSize(new Dimension(60, 30));

        JButton btnAddItem = btn("Add ➕", new Color(52,152,219));
        btnAddItem.addActionListener(e -> addToCart());

        itemAdder.add(new JLabel("Item:")); itemAdder.add(cbMenuItem);
        itemAdder.add(new JLabel("Qty:")); itemAdder.add(spnQty);
        itemAdder.add(btnAddItem);

        // Cart table
        String[] cols = {"#","Item","Qty","Unit Price","Subtotal"};
        cartModel = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        cartTable = new JTable(cartModel);
        cartTable.setFont(new Font("Segoe UI",Font.PLAIN,12));
        cartTable.setRowHeight(26);
        cartTable.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        cartTable.getTableHeader().setBackground(PRIMARY);
        cartTable.getTableHeader().setForeground(Color.WHITE);
        cartTable.setGridColor(new Color(220,220,220));
        int[] cw = {30,190,45,90,90};
        for(int i=0;i<cw.length;i++) cartTable.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);
        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setPreferredSize(new Dimension(0,180));
        cartScroll.setBorder(new LineBorder(new Color(200,200,200)));

        // Footer: total + buttons
        JPanel footer = new JPanel(new BorderLayout(10,5));
        footer.setOpaque(false);

        lblTotal = new JLabel("Total: Rs. 0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI",Font.BOLD,18));
        lblTotal.setForeground(new Color(39,174,96));

        JPanel footBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        footBtns.setOpaque(false);
        JButton btnRemove  = btn("❌ Remove Item", DANGER);
        JButton btnClear   = btn("🔄 Clear Cart", new Color(149,165,166));
        JButton btnPlace   = btn("✅ Place Order", SUCCESS);
        btnRemove.addActionListener(e -> removeFromCart());
        btnClear.addActionListener(e -> clearCart());
        btnPlace.addActionListener(e -> placeOrder());
        footBtns.add(btnRemove); footBtns.add(btnClear); footBtns.add(btnPlace);

        footer.add(lblTotal, BorderLayout.CENTER);
        footer.add(footBtns, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0,8));
        center.setOpaque(false);
        center.add(infoPanel, BorderLayout.NORTH);
        center.add(itemAdder, BorderLayout.CENTER);
        center.add(cartScroll, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    // ── Active Orders Panel ───────────────────────────────────────────
    private JPanel buildActiveOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0,8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200,200,200),1,true),
            BorderFactory.createEmptyBorder(18,18,18,18)));

        JLabel title = new JLabel("🔥 Active Orders");
        title.setFont(new Font("Segoe UI",Font.BOLD,17));
        title.setForeground(PRIMARY);

        String[] cols = {"ID","Customer","Table","Waiter","Status","Total (Rs.)","Date"};
        ordersModel = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        ordersTable = new JTable(ordersModel);
        ordersTable.setFont(new Font("Segoe UI",Font.PLAIN,12));
        ordersTable.setRowHeight(26);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));
        ordersTable.getTableHeader().setBackground(PRIMARY);
        ordersTable.getTableHeader().setForeground(Color.WHITE);
        ordersTable.setGridColor(new Color(220,220,220));
        ordersTable.setSelectionBackground(new Color(52,152,219,60));
        int[] cw = {40,130,65,110,90,100,140};
        for(int i=0;i<cw.length;i++) ordersTable.getColumnModel().getColumn(i).setPreferredWidth(cw[i]);

        // Color status column
        ordersTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                super.getTableCellRendererComponent(t,v,s,f,r,c);
                setHorizontalAlignment(CENTER);
                String st = v==null?"":v.toString();
                if(st.equals("PENDING"))   setForeground(new Color(241,196,15));
                else if(st.equals("PREPARING")) setForeground(new Color(52,152,219));
                else if(st.equals("SERVED"))    setForeground(new Color(39,174,96));
                else if(st.equals("PAID"))      setForeground(new Color(100,100,100));
                else if(st.equals("CANCELLED")) setForeground(DANGER);
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(ordersTable);
        scroll.setBorder(new LineBorder(new Color(200,200,200)));

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        controls.setOpaque(false);
        cbStatus = new JComboBox<>(Order.Status.values());
        cbStatus.setFont(new Font("Segoe UI",Font.PLAIN,12));
        JButton btnUpdateStatus = btn("Update Status", new Color(52,152,219));
        JButton btnViewBill     = btn("🧾 View Bill", new Color(155,89,182));
        JButton btnRefresh      = btn("🔄 Refresh", new Color(149,165,166));
        btnUpdateStatus.addActionListener(e -> updateOrderStatus());
        btnViewBill.addActionListener(e -> viewBill());
        btnRefresh.addActionListener(e -> loadActiveOrders());
        controls.add(new JLabel("Change Status:")); controls.add(cbStatus);
        controls.add(btnUpdateStatus); controls.add(btnViewBill); controls.add(btnRefresh);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    // ── Data loading ──────────────────────────────────────────────────
    private void loadCombos() {
        try {
            cbCustomer.removeAllItems();
            custDAO.getAllCustomers().forEach(cbCustomer::addItem);

            cbTable.removeAllItems();
            tableDAO.getAvailableTables().forEach(cbTable::addItem);

            cbWaiter.removeAllItems();
            empDAO.getAllEmployees().stream()
                .filter(e -> e.getRole()==Employee.Role.WAITER || e.getRole()==Employee.Role.MANAGER)
                .forEach(cbWaiter::addItem);

            cbMenuItem.removeAllItems();
            menuDAO.getAvailableItems().forEach(cbMenuItem::addItem);
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void loadActiveOrders() {
        ordersModel.setRowCount(0);
        try {
            for (Order o : orderDAO.getActiveOrders()) {
                ordersModel.addRow(new Object[]{
                    o.getId(),
                    o.getCustomerName() != null ? o.getCustomerName() : "Walk-in",
                    o.getTableNumber() > 0 ? "Table " + o.getTableNumber() : "-",
                    o.getEmployeeName() != null ? o.getEmployeeName() : "-",
                    o.getStatus().name(),
                    String.format("%.2f", o.getTotalAmount()),
                    o.getOrderDate()
                });
            }
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    // ── Cart actions ──────────────────────────────────────────────────
    private void addToCart() {
        MenuItem item = (MenuItem) cbMenuItem.getSelectedItem();
        if (item == null) return;
        int qty = (int) spnQty.getValue();

        // Check if already in cart, increase qty
        for (OrderItem oi : cart) {
            if (oi.getMenuItemId() == item.getId()) {
                oi.setQuantity(oi.getQuantity() + qty);
                refreshCartTable();
                return;
            }
        }
        cart.add(new OrderItem(0, item.getId(), item.getName(), qty, item.getPrice()));
        refreshCartTable();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row >= 0) { cart.remove(row); refreshCartTable(); }
    }

    private void clearCart() {
        cart.clear(); refreshCartTable();
    }

    private void refreshCartTable() {
        cartModel.setRowCount(0);
        double total = 0;
        for (int i = 0; i < cart.size(); i++) {
            OrderItem oi = cart.get(i);
            cartModel.addRow(new Object[]{
                i+1, oi.getMenuItemName(), oi.getQuantity(),
                String.format("%.2f", oi.getPrice()),
                String.format("%.2f", oi.getSubtotal())
            });
            total += oi.getSubtotal();
        }
        lblTotal.setText("Total: Rs. " + String.format("%.2f", total));
    }

    // ── Order actions ─────────────────────────────────────────────────
    private void placeOrder() {
        if (cart.isEmpty()) { showErr("Cart is empty. Add items first."); return; }
        Customer cust   = (Customer)        cbCustomer.getSelectedItem();
        RestaurantTable t = (RestaurantTable) cbTable.getSelectedItem();
        Employee waiter = (Employee)         cbWaiter.getSelectedItem();
        if (cust==null||t==null||waiter==null) { showErr("Please select customer, table, and waiter."); return; }

        Order order = new Order(cust.getId(), t.getId(), waiter.getId());
        order.setOrderItems(new ArrayList<>(cart));
        order.calculateTotal();

        try {
            int oid = orderDAO.createOrder(order);
            tableDAO.updateTableStatus(t.getId(), RestaurantTable.TableStatus.OCCUPIED);
            JOptionPane.showMessageDialog(this,
                "✅ Order #" + oid + " placed successfully!\nTotal: Rs. " + String.format("%.2f", order.getTotalAmount()),
                "Order Placed", JOptionPane.INFORMATION_MESSAGE);
            clearCart();
            loadCombos();
            loadActiveOrders();
            mainFrame.refreshAll();
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void updateOrderStatus() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) { showErr("Select an order first."); return; }
        int orderId = (int) ordersModel.getValueAt(row, 0);
        Order.Status newStatus = (Order.Status) cbStatus.getSelectedItem();
        try {
            orderDAO.updateOrderStatus(orderId, newStatus);
            // If paid/cancelled, free the table
            if (newStatus == Order.Status.PAID || newStatus == Order.Status.CANCELLED) {
                Order o = orderDAO.getOrderById(orderId);
                if (o != null && o.getTableId() > 0)
                    tableDAO.updateTableStatus(o.getTableId(), RestaurantTable.TableStatus.AVAILABLE);
            }
            loadActiveOrders();
            mainFrame.refreshAll();
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    private void viewBill() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) { showErr("Select an order to view bill."); return; }
        int orderId = (int) ordersModel.getValueAt(row, 0);
        try {
            Order o = orderDAO.getOrderById(orderId);
            if (o == null) return;
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════\n");
            sb.append("         🍽  RESTAURANT BILL\n");
            sb.append("              Developed by Saba\n");
            sb.append("═══════════════════════════════════════\n");
            sb.append(String.format("Order #:   %d%n", o.getId()));
            sb.append(String.format("Customer:  %s%n", o.getCustomerName() != null ? o.getCustomerName() : "Walk-in"));
            sb.append(String.format("Table:     %d%n", o.getTableNumber()));
            sb.append(String.format("Waiter:    %s%n", o.getEmployeeName() != null ? o.getEmployeeName() : "-"));
            sb.append(String.format("Date:      %s%n", o.getOrderDate()));
            sb.append(String.format("Status:    %s%n", o.getStatus()));
            sb.append("───────────────────────────────────────\n");
            sb.append(String.format("%-22s %4s  %10s%n","Item","Qty","Subtotal"));
            sb.append("───────────────────────────────────────\n");
            for (OrderItem item : o.getOrderItems()) {
                sb.append(String.format("%-22s %4d  Rs.%7.2f%n",
                    item.getMenuItemName(), item.getQuantity(), item.getSubtotal()));
            }
            sb.append("───────────────────────────────────────\n");
            sb.append(String.format("%-22s %4s  Rs.%7.2f%n","TOTAL","", o.getTotalAmount()));
            sb.append("═══════════════════════════════════════\n");
            sb.append("     Thank you for dining with us!\n");
            sb.append("═══════════════════════════════════════\n");

            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
            ta.setEditable(false);
            ta.setBackground(new Color(252,252,252));
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(440,400));
            JOptionPane.showMessageDialog(this, sp, "Bill - Order #"+orderId, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) { showErr(e.getMessage()); }
    }

    public void refresh() {
        loadCombos();
        loadActiveOrders();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI",Font.PLAIN,13));
        return l;
    }
    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI",Font.BOLD,12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void showErr(String msg) { JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE); }
}
