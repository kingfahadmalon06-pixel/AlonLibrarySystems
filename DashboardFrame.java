/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.alonlibrarysystem;

public class DashboardFrame extends javax.swing.JFrame {

    // Default Constructor
    public DashboardFrame() {
        initComponents();
    }

    // Constructor that accepts the student's surname (optional, if you pass surname from login)
    public DashboardFrame(String surname) {
        initComponents();
        // You can set a welcome label here if you have one, e.g.:
        // lblWelcome.setText("Welcome, " + surname);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        // NetBeans GUI Builder code goes here
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Student Dashboard");
        
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }
}