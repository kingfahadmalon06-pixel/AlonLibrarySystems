/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.alonlibrarysystem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import static org.neo4j.driver.Values.parameters;

/**
 * Student dashboard shown after a successful student login.
 */
public class StudentDashboard extends JFrame {

    private static final Color SIDEBAR_BG = Color.WHITE;
    private static final Color SIDEBAR_TEXT = new Color(17, 17, 17);
    private static final Color SIDEBAR_HOVER = new Color(240, 240, 240);
    private static final Color ACCENT = new Color(160, 30, 30);

    private static final Font TITLE_FONT = new Font("Serif", Font.BOLD | Font.ITALIC, 19);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font NAV_FONT = new Font("SansSerif", Font.BOLD, 15);

    private final String studentName;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private DefaultTableModel booksTableModel;
    private DefaultTableModel borrowTableModel;
    private DefaultTableModel borrowedTableModel;

    private JTable catalogTable;
    private JTable borrowTable;

    public StudentDashboard(String studentName) {
        this.studentName = studentName;

        setTitle("Alon Library System - Student Dashboard");
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 550));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize models before building layout
        initTableModels();

        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        contentPanel.add(buildBooksPanel(), "BOOKS");
        contentPanel.add(buildBorrowPanel(), "BORROW");
        contentPanel.add(buildReturnsPanel(), "RETURNS");

        cardLayout.show(contentPanel, "BOOKS");

        loadBooks();
    }

    private void initTableModels() {
        booksTableModel = new DefaultTableModel(
                new Object[]{"Book ID", "Title", "Author", "ISBN", "Category", "Available Copies", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        borrowTableModel = new DefaultTableModel(
                new Object[]{"Book ID", "Title", "Author", "ISBN", "Category", "Borrowed On", "Due Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        // Added "Rel ID" as the first column for precise single-item returns
        borrowedTableModel = new DefaultTableModel(
                new Object[]{"Rel ID", "Book ID", "Title", "Author", "ISBN", "Category", "Borrowed On", "Due Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    // ---------- Top bar ----------

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        top.setPreferredSize(new Dimension(0, 60));

        JLabel welcome = new JLabel(" Welcome, " + studentName);
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

        sidebar.add(navButton("Books", e -> {
            loadBooks();
            cardLayout.show(contentPanel, "BOOKS");
        }));
        sidebar.add(navButton("My Borrowed", e -> {
            loadBorrowedBooks();
            cardLayout.show(contentPanel, "BORROW");
        }));
        sidebar.add(navButton("Return Book", e -> {
            loadBorrowedBooks();
            cardLayout.show(contentPanel, "RETURNS");
        }));

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
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_HOVER); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_BG); }
        });
        return btn;
    }

    // ---------- Books Panel ----------

    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Available Books to Borrow");
        header.setFont(HEADER_FONT);
        panel.add(header, BorderLayout.NORTH);

        catalogTable = new JTable(booksTableModel);
        catalogTable.setRowHeight(26);
        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);

        JButton borrowBtn = new JButton("Borrow");
        borrowBtn.setBackground(ACCENT);
        borrowBtn.setForeground(Color.WHITE);
        borrowBtn.addActionListener(e -> executeBorrow(catalogTable, booksTableModel));

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadBooks());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setBackground(Color.WHITE);
        south.add(borrowBtn);
        south.add(refresh);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    // ---------- Borrow Panel ----------

    private JPanel buildBorrowPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel("Borrowed Books");
        header.setFont(HEADER_FONT);
        panel.add(header, BorderLayout.NORTH);

        borrowTable = new JTable(borrowTableModel);
        borrowTable.setRowHeight(26);
        panel.add(new JScrollPane(borrowTable), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadBorrowedBooks());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setBackground(Color.WHITE);
        south.add(refresh);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    // ---------- Borrow Action (Saves to Neo4j) ----------

    private void executeBorrow(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bookId = String.valueOf(model.getValueAt(row, 0));
        int avail = Integer.parseInt(String.valueOf(model.getValueAt(row, 5)));

        if (avail <= 0) {
            JOptionPane.showMessageDialog(this, "Sorry, this book is currently out of stock.",
                    "Unavailable", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusWeeks(2);

            session.executeWrite(tx -> {
                tx.run("MATCH (s:Student) WHERE s.surname = $surname OR s.name = $surname " +
                       "MATCH (b:Book) WHERE elementId(b) = $bookId OR b.isbn = $bookId OR b.id = $bookId " +
                       "WITH s, b WHERE b.availableCopies > 0 " +
                       "SET b.availableCopies = b.availableCopies - 1 " +
                       "CREATE (s)-[r:BORROWED {borrowDate: $borrowDate, dueDate: $dueDate}]->(b)",
                       parameters("bookId", bookId, "surname", studentName,
                                  "borrowDate", borrowDate.toString(), "dueDate", dueDate.toString()));
                return null;
            });

            JOptionPane.showMessageDialog(this, "Successfully borrowed!", "Book Borrowed", JOptionPane.INFORMATION_MESSAGE);

            loadBooks();
            loadBorrowedBooks();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not borrow book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBooks() {
        if (booksTableModel == null) return;
        booksTableModel.setRowCount(0);

        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            Result result = session.run("MATCH (b:Book) RETURN coalesce(b.id, elementId(b)) AS id, b.title AS title, b.author AS author, b.isbn AS isbn, b.category AS category, b.availableCopies AS availableCopies");

            while (result.hasNext()) {
                Record record = result.next();
                String id = record.get("id").asString("N/A");
                String title = record.get("title").asString("N/A");
                String author = record.get("author").asString("N/A");
                String isbn = record.get("isbn").asString("N/A");
                String category = record.get("category").asString("General");

                int availableCopies = record.get("availableCopies").asInt(0);
                String status = availableCopies > 0 ? "Available" : "Out of Stock";

                booksTableModel.addRow(new Object[]{id, title, author, isbn, category, availableCopies, status});
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load books: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Returns Panel ----------

    private JPanel buildReturnsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        panel.setBackground(Color.WHITE);

        JLabel header = new JLabel("My Borrowed Books");
        header.setFont(HEADER_FONT);
        panel.add(header, BorderLayout.NORTH);

        JTable table = new JTable(borrowedTableModel);
        table.setRowHeight(26);
        
        // Hide the Rel ID column (Index 0) so the user doesn't see it, but we can still use it behind the scenes
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton returnBtn = new JButton("Return Selected Book");
        returnBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a book to return first.",
                        "No selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get Rel ID and Book ID of the exact selected row
            String relId = String.valueOf(borrowedTableModel.getValueAt(row, 0));
            String bookId = String.valueOf(borrowedTableModel.getValueAt(row, 1));

            try (Driver driver = Neo4jConfig.getDriver();
                 Session session = driver.session()) {

                session.executeWrite(tx -> {
                    tx.run("MATCH (s:Student) WHERE s.surname = $surname OR s.name = $surname " +
                           "MATCH (b:Book) WHERE elementId(b) = $bookId OR b.isbn = $bookId OR b.id = $bookId " +
                           "MATCH (s)-[r:BORROWED]->(b) WHERE elementId(r) = $relId " +
                           "DELETE r " +
                           "SET b.availableCopies = b.availableCopies + 1",
                           parameters("surname", studentName, "bookId", bookId, "relId", relId));
                    return null;
                });

                JOptionPane.showMessageDialog(this, "Book returned successfully.");
                loadBooks();
                loadBorrowedBooks();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not return book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(Color.WHITE);
        south.add(returnBtn);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    // ---------- Fetch Active Loans directly from Neo4j ----------

    private void loadBorrowedBooks() {
        if (borrowTableModel == null && borrowedTableModel == null) return;

        if (borrowTableModel != null) borrowTableModel.setRowCount(0);
        if (borrowedTableModel != null) borrowedTableModel.setRowCount(0);

        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            Result result = session.run(
                    "MATCH (s:Student) WHERE s.surname = $surname OR s.name = $surname " +
                    "MATCH (s)-[r:BORROWED]->(b:Book) " +
                    "RETURN elementId(r) AS relId, coalesce(b.id, elementId(b)) AS id, b.title AS title, b.author AS author, b.isbn AS isbn, b.category AS category, r.borrowDate AS borrowDate, r.dueDate AS dueDate",
                    parameters("surname", studentName)
            );

            while (result.hasNext()) {
                Record record = result.next();
                String relId = record.get("relId").asString("");
                String bookId = record.get("id").asString("N/A");
                String title = record.get("title").asString("N/A");
                String author = record.get("author").asString("N/A");
                String isbn = record.get("isbn").asString("N/A");
                String category = record.get("category").asString("General");

                String rawBorrowDate = record.get("borrowDate").asString("N/A");
                String rawDueDate = record.get("dueDate").asString("N/A");

                String borrowDate = formatDate(rawBorrowDate);
                String dueDate = formatDate(rawDueDate);

                // For borrowTable (no Rel ID needed in columns)
                if (borrowTableModel != null) {
                    borrowTableModel.addRow(new Object[]{
                        bookId, title, author, isbn, category, borrowDate, dueDate
                    });
                }

                // For borrowedTableModel / Returns tab (includes Rel ID at index 0)
                if (borrowedTableModel != null) {
                    borrowedTableModel.addRow(new Object[]{
                        relId, bookId, title, author, isbn, category, borrowDate, dueDate
                    });
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not load active loans: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper method to convert yyyy-MM-dd to yyyy/MM/dd
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.equals("N/A") || dateStr.trim().isEmpty()) {
            return "N/A";
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        } catch (Exception e) {
            return dateStr;
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
        SwingUtilities.invokeLater(() -> new StudentDashboard("Alon").setVisible(true));
    }
}