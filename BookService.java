/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.alonlibrarysystem;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import static org.neo4j.driver.Values.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookService {

    // --- OVERLOADED METHOD: Uses Programming (5 total, 3 available) ---
    public void addBook(String title, String author) {
        addBook(title, author, "N/A", "Programming", 5, 3);
    }

    // --- Original 5-parameter method for backward compatibility ---
    public void addBook(String title, String author, String isbn, String category, int totalCopies) {
        addBook(title, author, isbn, category, totalCopies, totalCopies);
    }

    // --- Full Create Method (handles distinct totalCopies and availableCopies) ---
    public void addBook(String title, String author, String isbn, String category, int totalCopies, int availableCopies) {
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            String query = "CREATE (b:Book {" +
                           "title: $title, " +
                           "author: $author, " +
                           "isbn: $isbn, " +
                           "category: $category, " +
                           "totalCopies: $totalCopies, " +
                           "availableCopies: $availableCopies, " +
                           "status: $status})";

            session.run(query, parameters(
                "title", title,
                "author", author,
                "isbn", isbn,
                "category", category,
                "totalCopies", totalCopies,
                "availableCopies", availableCopies,
                "status", "Available"
            ));

            System.out.println("Book added successfully to Neo4j!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Read: Fetch all books to populate the JTable ---
    public List<Map<String, Object>> getAllBooks() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            Result result = session.run("MATCH (b:Book) RETURN b");
            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Node node = record.get("b").asNode();
                
                Map<String, Object> map = new HashMap<>(node.asMap());
                // Store elementId as 'id' so deletion/updates can reference it easily
                map.put("id", node.elementId());
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Read: Find a book by ISBN
    public Map<String, Object> findBookByIsbn(String isbn) {
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            Result result = session.run(
                "MATCH (b:Book {isbn: $isbn}) RETURN b",
                parameters("isbn", isbn)
            );

            if (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Node node = record.get("b").asNode();
                Map<String, Object> map = new HashMap<>(node.asMap());
                map.put("id", node.elementId());
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update: Modify available stock copies
    public void updateAvailableCopies(String isbn, int countChange) {
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            String query = "MATCH (b:Book {isbn: $isbn}) " +
                           "SET b.availableCopies = b.availableCopies + $countChange";
            session.run(query, parameters("isbn", isbn, "countChange", countChange));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Delete: Remove a book by its elementId or ISBN ---
    public void deleteBookById(String id) {
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            // Handles deletion using either Neo4j's internal elementId or fallback matching via ISBN
            String query = "MATCH (b:Book) WHERE elementId(b) = $id OR b.isbn = $id DELETE b";
            session.run(query, parameters("id", id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}