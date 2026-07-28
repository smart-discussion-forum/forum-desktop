package com.mindshare.database;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

 //SDD Section 3.1.1- Data Layer
public class SQLiteConnection {
    //Stored alongside the running app(mindshare-local.db)
    private static final String DB_URL = "jdbc:sqlite:mindshare-local.db";

    private static Connection connection;

    //Returns a single shared connection, creating it if it doesn't exist yet. SQLite creates the .db file automatically on first connect.
public static Connection getConnection() throws SQLException {
    if (connection == null || connection.isClosed()) {
        connection = DriverManager.getConnection(DB_URL);
    }
    return connection;
}

//Creates the local cache tables if they don't already exist.
   public static void initializeSchema()  {
    String createCachedMessagesTable = """
            CREATE TABLE IF NOT EXISTS cached_messages (
            local_id INTEGER PRIMARY KEY AUTOINCREMENT,
            sender_id INTEGER NOT NULL,
            group_id INTEGER NOT NULL,
            CONTENT TEXT NOT NULL,
            sent_at TEXT NOT NULL,
            is_synced INTEGER NOT NULL DEFAULT 0
            );
            """;
    String createPendingActionsTable = """
            CREATE TABLE IF NOT EXISTS pending_actions (
            action_id INTEGER PRIMARY KEY AUTOINCREMENT,
            method TEXT NOT NULL,
            path TEXT NOT NULL,
            body TEXT NOT NULL,
            auth_token TEXT,
            created_at TEXT NOT NULL,
            attempts INTEGER NOT NULL DEFAULT 0
            );
            """;
    try (Connection conn = getConnection();
    Statement stmt = conn.createStatement()) {
        stmt.execute(createCachedMessagesTable);
        stmt.execute(createPendingActionsTable);
        System.out.println("Local SQLite schema ready.");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }

   }

   public static void enqueuePendingPost(String path, String body, String token) {
       String sql = "INSERT INTO pending_actions (method, path, body, auth_token, created_at) VALUES (?, ?, ?, ?, datetime('now'))";
       try (Connection conn = getConnection(); java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
           stmt.setString(1, "POST");
           stmt.setString(2, path);
           stmt.setString(3, body);
           stmt.setString(4, token);
           stmt.executeUpdate();
       } catch (SQLException e) {
           e.printStackTrace();
       }
   }

   public static java.util.List<PendingAction> getPendingActions() {
       java.util.List<PendingAction> actions = new java.util.ArrayList<>();
       String sql = "SELECT action_id, method, path, body, auth_token FROM pending_actions ORDER BY action_id";
       try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
           while (rs.next()) {
               actions.add(new PendingAction(rs.getLong("action_id"), rs.getString("method"),
                       rs.getString("path"), rs.getString("body"), rs.getString("auth_token")));
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return actions;
   }

   public static void deletePendingAction(long actionId) {
       try (Connection conn = getConnection(); java.sql.PreparedStatement stmt = conn.prepareStatement(
               "DELETE FROM pending_actions WHERE action_id = ?")) {
           stmt.setLong(1, actionId);
           stmt.executeUpdate();
       } catch (SQLException e) {
           e.printStackTrace();
       }
   }

   public record PendingAction(long id, String method, String path, String body, String token) {}
   public static void insertCachedMessage(CachedMessage message) {
    String sql = "INSERT INTO cached_messages (sender_id, group_id, content, sent_at, is_synced)"
+ "VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
         java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, message.getSenderId());
        stmt.setInt(2,message.getGroupId());
        stmt.setString(3, message.getContent());
        stmt.setString(4, message.getSentAt());
        stmt.setInt(5, message.isSynced() ? 1 : 0);

        stmt.executeUpdate();
        System.out.println("Cached message inserted locally.");
    }
    catch (SQLException e) {
        e.printStackTrace();
    }
}
     public static void printAllCachedMessages() {
         String sql = "SELECT * FROM cached_messages";

         try (Connection conn = getConnection();
              Statement stmt = conn.createStatement();
              java.sql.ResultSet rs = stmt.executeQuery(sql)) {

             while (rs.next()) {
                 System.out.println(
                         "local_id=" + rs.getInt("local_id") +
                                 ", sender_id=" + rs.getInt("sender_id") +
                                 ", group_id=" + rs.getInt("group_id") +
                                 ", content=" + rs.getString("content") +
                                 ", sent_at=" + rs.getString("sent_at") +
                                 ", is_synced=" + rs.getInt("is_synced")
                 );
             }

         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
}
