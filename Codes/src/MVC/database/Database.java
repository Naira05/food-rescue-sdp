package MVC.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static Connection connection;

    private Database() {
    }

    public static synchronized void open(String url) throws DataAccessException {
        close();
        try {
            connection = DriverManager.getConnection(url);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
                st.execute("""
                        CREATE TABLE IF NOT EXISTS users (
                            id            INTEGER PRIMARY KEY AUTOINCREMENT,
                            role          TEXT NOT NULL
                                          CHECK (role IN ('DONOR','VOLUNTEER','BENEFICIARY','ADMIN')),
                            name          TEXT NOT NULL,
                            email         TEXT NOT NULL UNIQUE COLLATE NOCASE,
                            password_hash TEXT NOT NULL,
                            address       TEXT,
                            skills        TEXT,
                            availability  TEXT,
                            needs         TEXT
                        )""");
                st.execute("""
                        CREATE TABLE IF NOT EXISTS donations (
                            id            INTEGER PRIMARY KEY AUTOINCREMENT,
                            donor_id      INTEGER NOT NULL REFERENCES users(id),
                            type          TEXT NOT NULL,
                            description   TEXT,
                            quantity      REAL NOT NULL CHECK (quantity > 0),
                            unit          TEXT,
                            expiry_date   TEXT NOT NULL,
                            donation_date TEXT NOT NULL,
                            status        TEXT NOT NULL
                        )""");
                st.execute("""
                        CREATE TABLE IF NOT EXISTS assignments (
                            id           INTEGER PRIMARY KEY AUTOINCREMENT,
                            volunteer_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            task         TEXT NOT NULL
                        )""");
                st.execute("""
                        CREATE TABLE IF NOT EXISTS food_requests (
                            id             INTEGER PRIMARY KEY AUTOINCREMENT,
                            beneficiary_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            item           TEXT NOT NULL,
                            quantity       REAL NOT NULL CHECK (quantity > 0),
                            requested_on   TEXT NOT NULL
                        )""");
                st.execute("""
                        CREATE TABLE IF NOT EXISTS donation_types (
                            id   INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE COLLATE NOCASE
                        )""");
                // first run only: give the admin a few types to start with
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM donation_types")) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        st.executeUpdate("INSERT INTO donation_types(name) VALUES ('Cooked meals')");
                        st.executeUpdate("INSERT INTO donation_types(name) VALUES ('Produce')");
                        st.executeUpdate("INSERT INTO donation_types(name) VALUES ('Packaged food')");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Cannot open database " + url, e);
        }
    }

    public static synchronized Connection get() {
        if (connection == null) {
            throw new IllegalStateException("Call Database.open(...) before using the database");
        }
        return connection;
    }

    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nothing useful to do on close
            }
            connection = null;
        }

    }
}
