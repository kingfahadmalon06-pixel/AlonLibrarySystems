/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.alonlibrarysystem;

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

public class BorrowService {

    // Custom date format: MM/dd/yyyy (e.g., 08/24/2026)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // Issues a book to a student
    public void issueBook(String studentSurname, String bookId) throws Exception {
        // FIXED: Only open the session from Neo4jConfig.getDriver(). Do NOT close the driver!
        try (Session session = Neo4jConfig.getDriver().session()) {

            // 1. Find the book by elementId or ISBN
            String findBookQuery = "MATCH (b:Book) WHERE elementId(b) = $bookId OR b.isbn = $bookId RETURN b";
            Result bookResult = session.run(findBookQuery, parameters("bookId", bookId));

            if (!bookResult.hasNext()) {
                throw new Exception("Book not found with ID/ISBN: " + bookId);
            }

            Record bookRecord = bookResult.next();
            org.neo4j.driver.types.Node bookNode = bookRecord.get("b").asNode();
            int available = (int) bookNode.get("availableCopies").asLong(0);

            if (available <= 0) {
                throw new Exception("No available copies to issue.");
            }

            // Format dates as MM/dd/yyyy
            LocalDate today = LocalDate.now();
            String formattedToday = today.format(DATE_FORMATTER);
            String formattedDueDate = today.plusDays(14).format(DATE_FORMATTER);

            // 2. Reduce available copies and create a BorrowedRecord node with full book properties
            String issueQuery = 
                "MATCH (s:Student) WHERE s.surname = $studentSurname OR s.name = $studentSurname " +
                "MATCH (b:Book) WHERE elementId(b) = $bookId OR b.isbn = $bookId " +
                "SET b.availableCopies = b.availableCopies - 1 " +
                "CREATE (br:BorrowedRecord { " +
                "    studentSurname: $studentSurname, " +
                "    bookId: elementId(b), " +
                "    title: b.title, " +
                "    author: b.author, " +
                "    isbn: b.isbn, " +
                "    category: b.category, " +
                "    borrowedOn: $borrowedOn, " +
                "    dueDate: $dueDate, " +
                "    status: 'BORROWED' " +
                "}) " +
                "CREATE (s)-[:HAS_BORROW]->(br)-[:FOR_BOOK]->(b)";

            session.run(issueQuery, parameters(
                "studentSurname", studentSurname,
                "bookId", bookId,
                "borrowedOn", formattedToday,
                "dueDate", formattedDueDate
            ));

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    // Retrieves all active borrowed records for admin dashboards
    public List<Map<String, Object>> getAllBorrowedBooks() {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Session session = Neo4jConfig.getDriver().session()) {

            String query = "MATCH (br:BorrowedRecord) WHERE br.status = 'BORROWED' RETURN br";
            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Node node = record.get("br").asNode();
                Map<String, Object> map = new HashMap<>(node.asMap());
                map.put("id", node.elementId()); // Exposes Neo4j's elementId as the record ID reference
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Retrieves active borrowed records for a specific student dashboard
    public List<Map<String, Object>> getBorrowedBooksForStudent(String studentName) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Session session = Neo4jConfig.getDriver().session()) {

            String query = "MATCH (br:BorrowedRecord) WHERE br.studentSurname = $studentName AND br.status = 'BORROWED' RETURN br";
            Result result = session.run(query, parameters("studentName", studentName));

            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Node node = record.get("br").asNode();
                Map<String, Object> map = new HashMap<>(node.asMap());
                map.put("id", node.elementId());
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Marks a record as returned and increases available copies
    public void returnBook(String recordId) throws Exception {
        try (Session session = Neo4jConfig.getDriver().session()) {

            // 1. Find the borrow record by elementId
            String findQuery = "MATCH (br:BorrowedRecord) WHERE elementId(br) = $recordId RETURN br";
            Result result = session.run(findQuery, parameters("recordId", recordId));

            if (!result.hasNext()) {
                throw new Exception("Borrow record not found.");
            }

            Record record = result.next();
            org.neo4j.driver.types.Node brNode = record.get("br").asNode();
            String bookId = brNode.get("bookId").asString();
            String status = brNode.get("status").asString();

            if ("RETURNED".equals(status)) {
                throw new Exception("This book has already been returned.");
            }

            // 2. Update status to RETURNED and increment book available copies back
            String updateQuery = 
                "MATCH (br:BorrowedRecord) WHERE elementId(br) = $recordId " +
                "SET br.status = 'RETURNED' " +
                "WITH br " +
                "MATCH (b:Book) WHERE elementId(b) = $bookId OR b.isbn = $bookId " +
                "SET b.availableCopies = b.availableCopies + 1";

            session.run(updateQuery, parameters("recordId", recordId, "bookId", bookId));

        } catch (Exception e) {
            throw new Exception("Error processing return: " + e.getMessage());
        }
    }
}