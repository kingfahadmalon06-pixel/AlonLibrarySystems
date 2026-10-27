/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.alonlibrarysystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import static org.neo4j.driver.Values.parameters;

/**
 * Admin dashboard connected to Neo4j.
 */
public class AdminDashboard extends JFrame {

    private static final Color SIDEBAR_BG = Color.WHITE;
    private static final Color SIDEBAR_TEXT = new Color(17, 17, 17);
    private static final Color SIDEBAR_HOVER = new Color(240, 240, 240);
    private static final Color ACCENT = new Color(160, 30, 30);
    
    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD | Font.ITALIC, 19);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font NAV_FONT = new Font("SansSerif", Font.BOLD, 15);

    private final String adminName;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private DefaultTableModel booksTableModel;
    private DefaultTableModel allBorrowedTableModel;

    public AdminDashboard(String adminName) {
        this.adminName = adminName;

        setTitle("Alon Library System - Admin Dashboard");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 550));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(buildBooksPanel(), "BOOKS");
        contentPanel.add(buildBorrowPanel(), "BORROW");
        contentPanel.add(buildReturnsPanel(), "RETURNS");

        cardLayout.show(contentPanel, "BOOKS");
    }

    // ---------- Top bar ----------

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        top.setPreferredSize(new Dimension(0, 60));

        JLabel welcome = new JLabel("   Admin: " + adminName);
        welcome.setFont(HEADER_FONT);
        top.add(welcome, BorderLayout.WEST);

        return top;
    }

    // ---------- Sidebar ----------

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));

        JLabel title = new JLabel("Alon Library");
        title.setFont(TITLE_FONT);
        title.setForeground(SIDEBAR_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 25, 20));
        sidebar.add(title);

        sidebar.add(navButton("Books", e -> cardLayout.show(contentPanel, "BOOKS")));
        sidebar.add(navButton("Borrow", e -> cardLayout.show(contentPanel, "BORROW")));
        sidebar.add(navButton("Returns", e -> cardLayout.show(contentPanel, "RETURNS")));

        sidebar.add(Box.createVerticalGlue());

        JButton logout = navButton("Logout", e -> handleLogout());
        logout.setForeground(new Color(200, 40, 40));
        sidebar.add(logout);
        sidebar.add(Box.createVerticalStrut(15));

        return sidebar;
    }

    private JButton navButton(String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(NAV_FONT);
        btn.setForeground(SIDEBAR_TEXT);
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.addActionListener(listener);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_BG); }
        });
        return btn;
    }

    // ---------- Books panel ----------

    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Manage Books");
        header.setFont(HEADER_FONT);
        panel.add(header, BorderLayout.NORTH);

        booksTableModel = new DefaultTableModel(
                new Object[]{"Book ID", "Title", "Author", "ISBN", "Category", "Total Copies", "Available Copies", "Status"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(booksTableModel);
        table.setRowHeight(26);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);

        JPanel inputGrid = new JPanel(new GridLayout(2, 6, 8, 8));
        inputGrid.setBackground(Color.WHITE);

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField totalCopiesField = new JTextField("5");
        JTextField availableCopiesField = new JTextField("3");

        inputGrid.add(new JLabel("Title:"));
        inputGrid.add(titleField);
        inputGrid.add(new JLabel("Author:"));
        inputGrid.add(authorField);
        inputGrid.add(new JLabel("ISBN:"));
        inputGrid.add(isbnField);

        inputGrid.add(new JLabel("Category:"));
        inputGrid.add(categoryField);
        inputGrid.add(new JLabel("Total:"));
        inputGrid.add(totalCopiesField);
        inputGrid.add(new JLabel("Available:"));
        inputGrid.add(availableCopiesField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.setBackground(Color.WHITE);

        JButton addBtn = new JButton("Add Book");
        addBtn.setBackground(ACCENT);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String a = authorField.getText().trim();
            String isbn = isbnField.getText().trim();
            String cat = categoryField.getText().trim();
            String totalStr = totalCopiesField.getText().trim();
            String availStr = availableCopiesField.getText().trim();

            if (t.isEmpty() || a.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter both title and author.",
                        "Missing info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int total = totalStr.isEmpty() ? 5 : Integer.parseInt(totalStr);
                int avail = availStr.isEmpty() ? 3 : Integer.parseInt(availStr);
                if (isbn.isEmpty()) isbn = "N/A";
                if (cat.isEmpty()) cat = "Programming";
                String bookId = "B" + System.currentTimeMillis();

                try (Driver driver = Neo4jConfig.getDriver();
                     Session session = driver.session()) {
                    String query = "CREATE (b:Book {bookId: $bookId, title: $title, author: $author, isbn: $isbn, category: $category, totalCopies: $total, availableCopies: $avail, status: 'Available'})";
                    session.run(query, parameters(
                        "bookId", bookId,
                        "title", t,
                        "author", a,
                        "isbn", isbn,
                        "category", cat,
                        "total", total,
                        "avail", avail
                    ));
                }

                JOptionPane.showMessageDialog(this, "Book added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                
                titleField.setText("");
                authorField.setText("");
                isbnField.setText("");
                categoryField.setText("");
                totalCopiesField.setText("5");
                availableCopiesField.setText("3");

                loadBooks();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Total and Available copies must be valid numbers.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not add book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a book to delete first.",
                        "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String bookId = String.valueOf(booksTableModel.getValueAt(row, 0));
            try (Driver driver = Neo4jConfig.getDriver();
                 Session session = driver.session()) {
                session.run("MATCH (b:Book {bookId: $bookId}) DELETE b", parameters("bookId", bookId));

                JOptionPane.showMessageDialog(this, "Book deleted successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBooks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not delete book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadBooks());

        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refresh);

        formContainer.add(inputGrid, BorderLayout.CENTER);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(formContainer, BorderLayout.SOUTH);

        loadBooks();
        return panel;
    }

    private void loadBooks() {
        booksTableModel.setRowCount(0);
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {
            Result result = session.run("MATCH (b:Book) RETURN b.bookId AS id, b.title AS title, b.author AS author, b.isbn AS isbn, b.category AS category, b.totalCopies AS totalCopies, b.availableCopies AS availableCopies, b.status AS status");
            while (result.hasNext()) {
                Record record = result.next();
                String id = record.get("id").asString("N/A");
                String title = record.get("title").asString("");
                String author = record.get("author").asString("");
                String isbn = record.get("isbn").asString("N/A");
                String category = record.get("category").asString("Programming");
                int totalCopies = record.get("totalCopies").asInt(5);
                int availableCopies = record.get("availableCopies").asInt(3);
                String status = record.get("status").asString("Available");

                booksTableModel.addRow(new Object[]{id, title, author, isbn, category, totalCopies, availableCopies, status});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load books: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Borrow panel ----------

    private JPanel buildBorrowPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Issue a Book");
        header.setFont(HEADER_FONT);
        panel.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField studentField = new JTextField(20);
        JTextField bookIdField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Student Surname:"), gbc);
        gbc.gridx = 1;
        form.add(studentField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1;
        form.add(bookIdField, gbc);

        JButton issueBtn = new JButton("Issue Book");
        issueBtn.setBackground(ACCENT);
        issueBtn.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 2;
        form.add(issueBtn, gbc);

        issueBtn.addActionListener(e -> {
            String student = studentField.getText().trim();
            String bookId = bookIdField.getText().trim();
            if (student.isEmpty() || bookId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter both student surname and book ID.",
                        "Missing info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (Driver driver = Neo4jConfig.getDriver();
                 Session session = driver.session()) {
                
                String query = "MATCH (s:Student {surname: $student}), (b:Book {bookId: $bookId}) " +
                               "WHERE b.availableCopies > 0 " +
                               "SET b.availableCopies = b.availableCopies - 1 " +
                               "CREATE (s)-[r:BORROWED {borrowedOn: $borrowedOn, dueDate: $dueDate}]->(b) " +
                               "RETURN r";
                
                Result res = session.run(query, parameters(
                    "student", student,
                    "bookId", bookId,
                    "borrowedOn", LocalDate.now().toString(),
                    "dueDate", LocalDate.now().plusDays(14).toString()
                ));

                if (!res.hasNext()) {
                    JOptionPane.showMessageDialog(this, "Could not issue book. Check student surname, book ID, or available copies.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JOptionPane.showMessageDialog(this, "Book issued successfully.");
                studentField.setText("");
                bookIdField.setText("");
                loadBooks();
                loadAllBorrowedBooks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not issue book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    // ---------- Returns panel ----------

    private JPanel buildReturnsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel("All Borrowed Books");
        header.setFont(HEADER_FONT);
        panel.add(header, BorderLayout.NORTH);

        // Added Rel ID at index 0
        allBorrowedTableModel = new DefaultTableModel(
                new Object[]{"Rel ID", "Student", "Book ID", "Title", "Borrowed On", "Due Date"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(allBorrowedTableModel);
        table.setRowHeight(26);

        // Hide the Rel ID column so the admin only sees Student, Book ID, Title, etc.
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton returnBtn = new JButton("Mark Selected as Returned");
        returnBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a record to return first.",
                        "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get Rel ID, Student, and Book ID from the selected row
            String relId = String.valueOf(allBorrowedTableModel.getValueAt(row, 0));
            String student = String.valueOf(allBorrowedTableModel.getValueAt(row, 1));
            String bookId = String.valueOf(allBorrowedTableModel.getValueAt(row, 2));

            try (Driver driver = Neo4jConfig.getDriver();
                 Session session = driver.session()) {
                
                String query = "MATCH (s:Student {surname: $student}) " +
                               "MATCH (b:Book {bookId: $bookId}) " +
                               "MATCH (s)-[r:BORROWED]->(b) WHERE elementId(r) = $relId " +
                               "DELETE r " +
                               "SET b.availableCopies = b.availableCopies + 1";
                
                session.run(query, parameters("student", student, "bookId", bookId, "relId", relId));

                JOptionPane.showMessageDialog(this, "Book marked as returned.");
                loadBooks();
                loadAllBorrowedBooks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not process return: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(Color.WHITE);
        south.add(returnBtn);
        panel.add(south, BorderLayout.SOUTH);

        loadAllBorrowedBooks();
        return panel;
    }

    private void loadAllBorrowedBooks() {
        allBorrowedTableModel.setRowCount(0);
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {
            
            Result result = session.run("MATCH (s:Student)-[r:BORROWED]->(b:Book) RETURN elementId(r) AS relId, s.surname AS student, b.bookId AS bookId, b.title AS title, r.borrowedOn AS borrowedOn, r.dueDate AS dueDate");
            
            while (result.hasNext()) {
                Record record = result.next();
                String relId = record.get("relId").asString("");
                String student = record.get("student").asString("");
                String bookId = record.get("bookId").asString("");
                String title = record.get("title").asString("");
                String borrowedOn = record.get("borrowedOn").asString("");
                String dueDate = record.get("dueDate").asString("");

                allBorrowedTableModel.addRow(new Object[]{relId, student, bookId, title, borrowedOn, dueDate});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load borrowed books: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Logout ----------

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginScreen().setVisible(true);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard("Test Admin").setVisible(true));
    }
}