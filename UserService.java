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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class handling Neo4j graph operations for User accounts and Borrowing/Return logic.
 */
public class UserService {

    public UserService() {
        // Uses Neo4jConfig driver connection
    }

    /**
     * Finds a single user by name or email.
     */
    public Map<String, Object> getUserByNameOrEmail(String identifier) {
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            String query = "MATCH (u:User) WHERE u.name = $identifier OR u.email = $identifier RETURN u LIMIT 1";
            Result result = session.run(query, parameters("identifier", identifier));

            if (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Node node = record.get("u").asNode();
                Map<String, Object> map = new HashMap<>(node.asMap());
                map.put("id", node.elementId());
                return map;
            }
        } catch (Exception e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all users who have active (unreturned) borrowed books.
     */
    public List<Map<String, Object>> getAllUsersWithActiveLoans() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            // Finds users connected via an active BORROWED relationship
            String query = "MATCH (u:User)-[r:BORROWED]->(b:Book) WHERE r.returned = false RETURN DISTINCT u";
            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Node node = record.get("u").asNode();
                Map<String, Object> map = new HashMap<>(node.asMap());
                map.put("id", node.elementId());
                resultList.add(map);
            }
        } catch (Exception e) {
            System.err.println("Error fetching active loans: " + e.getMessage());
        }
        return resultList;
    }

    /**
     * Issues a book to a student by creating a graph relationship and reducing available copies.
     */
    public void issueBookToStudent(String studentIdentifier, String bookIdStr) throws Exception {
        String cleanBookId = sanitizeObjectIdString(bookIdStr);

        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            // 1. Find the book by elementId or ISBN
            String findBookQuery = "MATCH (b:Book) WHERE elementId(b) = $bookId OR b.isbn = $bookId RETURN b";
            Result bookResult = session.run(findBookQuery, parameters("bookId", cleanBookId));

            if (!bookResult.hasNext()) {
                throw new Exception("Book with ID " + cleanBookId + " not found.");
            }

            Record bookRecord = bookResult.next();
            org.neo4j.driver.types.Node bookNode = bookRecord.get("b").asNode();
            int availableCopies = bookNode.containsKey("availableCopies") ? (int) bookNode.get("availableCopies").asLong(0) : 0;

            if (availableCopies <= 0) {
                throw new Exception("This book is currently out of stock.");
            }

            // 2. Fetch or create student/user node
            Map<String, Object> user = getUserByNameOrEmail(studentIdentifier);
            if (user == null) {
                String email = studentIdentifier.toLowerCase().replace(" ", "") + "@example.com";
                String createUserQuery = "CREATE (u:User {name: $name, email: $email, role: 'student'}) RETURN u";
                session.run(createUserQuery, parameters("name", studentIdentifier, "email", email));
            }

            // 3. Prepare dates and book details
            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(14);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String title = bookNode.containsKey("title") ? bookNode.get("title").asString() : "Unknown";
            String author = bookNode.containsKey("author") ? bookNode.get("author").asString() : "N/A";
            String isbn = bookNode.containsKey("isbn") ? bookNode.get("isbn").asString() : "N/A";
            String category = bookNode.containsKey("category") ? bookNode.get("category").asString() : "General";

            // 4. Create the BORROWED relationship safely and decrement book stock
            String issueQuery = 
                "MATCH (u:User {name: $studentIdentifier}) " +
                "MATCH (b:Book) WHERE elementId(b) = $bookId OR b.isbn = $bookId " +
                "SET b.availableCopies = b.availableCopies - 1 " +
                "CREATE (u)-[r:BORROWED]->(b) " +
                "SET r.bookId = elementId(b), " +
                "    r.title = $title, " +
                "    r.author = $author, " +
                "    r.isbn = $isbn, " +
                "    r.category = $category, " +
                "    r.borrowDate = $borrowDate, " +
                "    r.dueDate = $dueDate, " +
                "    r.returned = false";

            session.run(issueQuery, parameters(
                "studentIdentifier", studentIdentifier,
                "bookId", cleanBookId,
                "title", title,
                "author", author,
                "isbn", isbn,
                "category", category,
                "borrowDate", today.format(fmt),
                "dueDate", dueDate.format(fmt)
            ));
        }
    }

    /**
     * Processes a book return by setting returned=true in Neo4j and increasing available copies.
     */
    public void processBookReturn(String studentName, String bookIdStr) throws Exception {
        String cleanBookId = sanitizeObjectIdString(bookIdStr);

        try (Driver driver = Neo4jConfig.getDriver();
             Session session = driver.session()) {

            // Match active borrow relationship, update status to returned, and increment stock
            String returnQuery = 
                "MATCH (u:User {name: $studentName})-[r:BORROWED]->(b:Book) " +
                "WHERE (elementId(b) = $bookId OR b.isbn = $bookId) AND r.returned = false " +
                "SET r.returned = true, " +
                "    b.availableCopies = b.availableCopies + 1 " +
                "RETURN count(r) AS modifiedCount";

            Result result = session.run(returnQuery, parameters(
                "studentName", studentName,
                "bookId", cleanBookId
            ));

            if (result.hasNext()) {
                long modifiedCount = result.next().get("modifiedCount").asLong(0);
                if (modifiedCount == 0) {
                    throw new Exception("No active loan found for " + studentName + " with this Book ID.");
                }
            } else {
                throw new Exception("No active loan found for " + studentName + " with this Book ID.");
            }
        }
    }

    /**
     * Builds a filter identifier representation (preserved for compatibility).
     */
    private String buildIdFilter(String idStr) {
        return sanitizeObjectIdString(idStr);
    }

    /**
     * Helper to clean trailing '...' or spaces attached by UI formatters.
     */
    private String sanitizeObjectIdString(String idStr) {
        if (idStr == null) return "";
        String clean = idStr.trim();
        if (clean.endsWith("...")) {
            clean = clean.substring(0, clean.length() - 3).trim();
        }
        return clean;
    }
}