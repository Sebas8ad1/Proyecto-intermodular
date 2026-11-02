package es.glaux.data;

import es.glaux.model.StudySession;
import es.glaux.model.Subject;
import es.glaux.model.Topic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Handles all communication with the SQLite database. */
public class StudyRepository {
    private final String url;

    public StudyRepository() throws SQLException {
        String home = System.getProperty("user.home");
        java.nio.file.Path folder = java.nio.file.Path.of(home, ".estudiafp");
        try {
            java.nio.file.Files.createDirectories(folder);
        } catch (java.io.IOException e) {
            throw new SQLException("No se ha podido crear la carpeta de datos de EstudiaFP.", e);
        }
        url = "jdbc:sqlite:" + folder.resolve("estudiafp.db");
        createTables();
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private void createTables() throws SQLException {
        String subjects = "CREATE TABLE IF NOT EXISTS subjects (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, description TEXT NOT NULL DEFAULT '')";
        String topics = "CREATE TABLE IF NOT EXISTS topics (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, subject_id INTEGER NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL DEFAULT '', " +
                "FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE, UNIQUE(subject_id, name))";
        String sessions = "CREATE TABLE IF NOT EXISTS study_sessions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, topic_id INTEGER NOT NULL, study_date TEXT NOT NULL, minutes INTEGER NOT NULL CHECK(minutes > 0), notes TEXT NOT NULL DEFAULT '', " +
                "FOREIGN KEY(topic_id) REFERENCES topics(id) ON DELETE CASCADE)";
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(subjects);
            statement.execute(topics);
            statement.execute(sessions);
        }
    }

    public List<Subject> getSubjects() throws SQLException {
        List<Subject> result = new ArrayList<>();
        String sql = "SELECT id, name, description FROM subjects ORDER BY name";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Subject(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
        }
        return result;
    }

    public void addSubject(String name, String description) throws SQLException {
        String sql = "INSERT INTO subjects(name, description) VALUES(?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim()); ps.setString(2, description.trim()); ps.executeUpdate();
        }
    }

    public void deleteSubject(int id) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public List<Topic> getTopics() throws SQLException {
        List<Topic> result = new ArrayList<>();
        String sql = "SELECT t.id, t.subject_id, s.name AS subject_name, t.name, t.description " +
                "FROM topics t JOIN subjects s ON s.id = t.subject_id ORDER BY s.name, t.name";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Topic(rs.getInt("id"), rs.getInt("subject_id"),
                    rs.getString("subject_name"), rs.getString("name"), rs.getString("description")));
        }
        return result;
    }

    public void addTopic(int subjectId, String name, String description) throws SQLException {
        String sql = "INSERT INTO topics(subject_id, name, description) VALUES(?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, subjectId); ps.setString(2, name.trim()); ps.setString(3, description.trim()); ps.executeUpdate();
        }
    }

    public void deleteTopic(int id) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM topics WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    public List<StudySession> getSessions() throws SQLException {
        List<StudySession> result = new ArrayList<>();
        String sql = "SELECT ss.id, ss.topic_id, t.name AS topic_name, s.name AS subject_name, " +
                "ss.study_date, ss.minutes, ss.notes FROM study_sessions ss " +
                "JOIN topics t ON t.id = ss.topic_id JOIN subjects s ON s.id = t.subject_id " +
                "ORDER BY ss.study_date DESC, ss.id DESC";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new StudySession(rs.getInt("id"), rs.getInt("topic_id"),
                    rs.getString("topic_name"), rs.getString("subject_name"), rs.getString("study_date"),
                    rs.getInt("minutes"), rs.getString("notes")));
        }
        return result;
    }

    public void addSession(int topicId, String date, int minutes, String notes) throws SQLException {
        String sql = "INSERT INTO study_sessions(topic_id, study_date, minutes, notes) VALUES(?, ?, ?, ?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, topicId); ps.setString(2, date); ps.setInt(3, minutes); ps.setString(4, notes.trim()); ps.executeUpdate();
        }
    }

    public void deleteSession(int id) throws SQLException {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM study_sessions WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }
}
