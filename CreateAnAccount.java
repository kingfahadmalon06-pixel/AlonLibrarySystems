/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.alonlibrarysystem;

import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import static org.neo4j.driver.Values.parameters;

public class CreateAnAccount extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CreateAnAccount.class.getName());

    public CreateAnAccount() {
        initComponents();
        jLabelStatus.setText(" ");
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jLabelTitle = new javax.swing.JLabel();
        jLabelHeading = new javax.swing.JLabel();
        jLabelSurname = new javax.swing.JLabel();
        jTextFieldSurname = new javax.swing.JTextField();
        jLabelFirstName = new javax.swing.JLabel();
        jTextFieldFirstName = new javax.swing.JTextField();
        jLabelPassword = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabelConfirmPassword = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();
        jLabelStatus = new javax.swing.JLabel();
        jButtonRegister = new javax.swing.JButton();
        jButtonBack = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabelTitle.setFont(new java.awt.Font("Arial", 2, 14));
        jLabelTitle.setText("Alon Library System");

        jLabelHeading.setFont(new java.awt.Font("Segoe UI", 1, 24));
        jLabelHeading.setText("Create student account");

        jLabelSurname.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12));
        jLabelSurname.setText("Surname");

        jLabelFirstName.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12));
        jLabelFirstName.setText("First name");

        jLabelPassword.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12));
        jLabelPassword.setText("Password");

        jLabelConfirmPassword.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12));
        jLabelConfirmPassword.setText("Confirm password");

        jLabelStatus.setForeground(new java.awt.Color(204, 0, 0));
        jLabelStatus.setText(" ");

        jButtonRegister.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12));
        jButtonRegister.setText("Register");
        jButtonRegister.addActionListener(this::jButtonRegisterActionPerformed);

        jButtonBack.setFont(new java.awt.Font("Segoe UI Semibold", 0, 12));
        jButtonBack.setText("Back to login");
        jButtonBack.addActionListener(this::jButtonBackActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelTitle)
                    .addComponent(jLabelHeading)
                    .addComponent(jLabelSurname)
                    .addComponent(jTextFieldSurname, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                    .addComponent(jLabelFirstName)
                    .addComponent(jTextFieldFirstName)
                    .addComponent(jLabelPassword)
                    .addComponent(jPasswordField1)
                    .addComponent(jLabelConfirmPassword)
                    .addComponent(jPasswordField2)
                    .addComponent(jLabelStatus)
                    .addComponent(jButtonRegister)
                    .addComponent(jButtonBack))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabelTitle)
                .addGap(6, 6, 6)
                .addComponent(jLabelHeading)
                .addGap(20, 20, 20)
                .addComponent(jLabelSurname)
                .addGap(4, 4, 4)
                .addComponent(jTextFieldSurname, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabelFirstName)
                .addGap(4, 4, 4)
                .addComponent(jTextFieldFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabelPassword)
                .addGap(4, 4, 4)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabelConfirmPassword)
                .addGap(4, 4, 4)
                .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jLabelStatus)
                .addGap(16, 16, 16)
                .addComponent(jButtonRegister)
                .addGap(10, 10, 10)
                .addComponent(jButtonBack)
                .addGap(24, 24, 24))
        );

        pack();
    }

    private void jButtonRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        String surname = jTextFieldSurname.getText().trim();
        String firstName = jTextFieldFirstName.getText().trim();
        String password = new String(jPasswordField1.getPassword());
        String confirmPassword = new String(jPasswordField2.getPassword());

        if (surname.isEmpty() || password.isEmpty()) {
            jLabelStatus.setText("Surname and password are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            jLabelStatus.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            jLabelStatus.setText("Password must be at least 6 characters.");
            return;
        }

        // Session lang ang kinuha para hindi masara ang shared singleton driver
        try (Session session = Neo4jConfig.getDriver().session()) {

            // Check if an account with that surname already exists
            String checkQuery = "MATCH (s:Student {surname: $surname}) RETURN s.surname AS surname";
            Result checkResult = session.run(checkQuery, parameters("surname", surname));

            if (checkResult.hasNext()) {
                jLabelStatus.setText("An account with that surname already exists.");
                return;
            }

            // Create the new student node in Neo4j
            String createQuery = "CREATE (s:Student {surname: $surname, firstName: $firstName, password: $password, role: $role})";
            session.run(createQuery, parameters(
                "surname", surname,
                "firstName", firstName,
                "password", Passwordutil.hash(password),
                "role", "student"
            ));

            javax.swing.JOptionPane.showMessageDialog(this,
                    "Account created! You can now log in.",
                    "Success",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setLocationRelativeTo(null);
            loginScreen.setVisible(true);
            this.dispose();

        } catch (Exception e) {
            logger.severe("Registration failed: " + e.getMessage());
            jLabelStatus.setText("Could not connect to the database.");
        }
    }

    private void jButtonBackActionPerformed(java.awt.event.ActionEvent evt) {
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setLocationRelativeTo(null);
        loginScreen.setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            CreateAnAccount screen = new CreateAnAccount();
            screen.setLocationRelativeTo(null);
            screen.setVisible(true);
        });
    }

    // Variables declaration
    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelHeading;
    private javax.swing.JLabel jLabelSurname;
    private javax.swing.JTextField jTextFieldSurname;
    private javax.swing.JLabel jLabelFirstName;
    private javax.swing.JTextField jTextFieldFirstName;
    private javax.swing.JLabel jLabelPassword;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JLabel jLabelConfirmPassword;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JButton jButtonRegister;
    private javax.swing.JButton jButtonBack;
    // End of variables declaration
}