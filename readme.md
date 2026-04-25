# 🍽 Restaurant Management System

> **Developed by Saba**  
> Java OOP + SQLite + Java Swing GUI

---

## 📌 Project Overview

A fully-featured **Restaurant Management System** built in **Java** using **Object-Oriented Programming** principles, **SQLite** for persistent data storage, and **Java Swing** for the graphical user interface.

---

## ✨ Features

| Module | Features |
|---|---|
| 📊 **Dashboard** | Live stats: today's orders, revenue, active orders, table availability |
| 🍽 **Menu Management** | Add / Edit / Delete menu items with category, price, availability |
| 🛒 **Order Management** | Create orders with cart, assign table & waiter, update status, generate bill |
| 🪑 **Table Management** | Add/edit tables, track status (Available / Occupied / Reserved) |
| 👥 **Customer Management** | Add/edit/search customers |
| 👷 **Employee Management** | Manage staff with roles (Manager, Chef, Waiter, Cashier, Cleaner) |

---

## 🏗 Project Structure

```
RestaurantManagementSystem/
│
├── src/com/saba/restaurant/
│   ├── Main.java                      ← Entry point
│   │
│   ├── models/                        ← OOP Data Models
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Customer.java
│   │   ├── Employee.java
│   │   └── RestaurantTable.java
│   │
│   ├── dao/                           ← Data Access Objects (SQL)
│   │   ├── MenuDAO.java
│   │   ├── OrderDAO.java
│   │   ├── CustomerDAO.java
│   │   ├── EmployeeDAO.java
│   │   └── TableDAO.java
│   │
│   ├── gui/                           ← Swing GUI Panels
│   │   ├── MainFrame.java
│   │   ├── DashboardPanel.java
│   │   ├── MenuPanel.java
│   │   ├── OrderPanel.java
│   │   ├── TablePanel.java
│   │   ├── CustomerPanel.java
│   │   └── EmployeePanel.java
│   │
│   └── utils/
│       ├── DatabaseConnection.java    ← Singleton DB connection
│       └── DatabaseInitializer.java   ← Schema creation + seed data
│
├── lib/
│   └── sqlite-jdbc-<version>.jar      ← Place SQLite JDBC driver here
│
└── README.md
```

---

## 🛠 OOP Concepts Used

- **Encapsulation** – All model fields are private with getters/setters
- **Inheritance** – Swing panels extend `JPanel`
- **Polymorphism** – Enum types for `Order.Status`, `Employee.Role`, `RestaurantTable.TableStatus`
- **Abstraction** – DAO layer abstracts all SQL from GUI
- **Singleton Pattern** – `DatabaseConnection` ensures a single DB connection
- **MVC-like Architecture** – Models, DAO (data layer), GUI (view/controller)

---

## 🗄 Database Schema (SQLite)

```sql
menu_items   (id, name, category, price, description, available)
customers    (id, name, phone, email, address, created_at)
employees    (id, name, role, phone, email, salary, hired_date)
tables       (id, table_number, capacity, status)
orders       (id, customer_id, table_id, employee_id, status, total_amount, order_date)
order_items  (id, order_id, menu_item_id, quantity, price)
```

---

## ⚙️ How to Run

### Prerequisites
- Java JDK 17 or later
- SQLite JDBC Driver

### Step 1 – Download SQLite JDBC Driver
Download from: https://github.com/xerial/sqlite-jdbc/releases  
Place the JAR file in the `lib/` folder.

### Step 2 – Compile

```bash
# Windows
javac -cp "lib/sqlite-jdbc-*.jar" -d out -sourcepath src src/com/saba/restaurant/Main.java

# Linux / macOS
javac -cp "lib/sqlite-jdbc-*.jar" -d out -sourcepath src src/com/saba/restaurant/Main.java
```

### Step 3 – Run

```bash
# Windows
java -cp "out;lib/sqlite-jdbc-*.jar" com.saba.restaurant.Main

# Linux / macOS
java -cp "out:lib/sqlite-jdbc-*.jar" com.saba.restaurant.Main
```

> 💡 The database file `restaurant.db` will be created automatically in the working directory on first run, with sample menu items, employees, and tables pre-loaded.

---

### Using an IDE (IntelliJ IDEA / Eclipse)

1. Open the project folder
2. Add `lib/sqlite-jdbc-*.jar` as a project library
3. Run `Main.java`

---

## 📸 Application Modules

### Dashboard
Real-time statistics cards showing today's performance at a glance.

### Menu Management
Full CRUD interface with search/filter. Manage item availability per shift.

### Order Management
- Select customer, table, and waiter
- Add multiple items to a cart with quantity control
- Place order → auto-marks table as Occupied
- Update order status: Pending → Preparing → Served → Paid
- Generate formatted bill receipt

### Table Management
Visual table grid with colour-coded status. Mark tables Available / Occupied / Reserved / Cleaning.

---

## 👩‍💻 Author

**Saba**  
GitHub: [your-github-username]

---

## 📄 License

This project is open-source and free to use for educational purposes.