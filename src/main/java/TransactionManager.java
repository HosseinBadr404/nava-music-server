import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {
    private UserManager userManager;
    private MusicManager musicManager;

    public TransactionManager(UserManager userManager, MusicManager musicManager) {
        this.userManager = userManager;
        this.musicManager = musicManager;
    }

    public boolean purchaseMusic(String userEmail, String musicTitle) {
        User user = userManager.getUserByEmail(userEmail);
        Music music = musicManager.getMusicByTitle(musicTitle);

        if (user == null || music == null) {
            return false;
        }

        // Check if user already has access
        if (userManager.canAccessMusic(userEmail, musicTitle)) {
            // Only add to transaction history, no need to add to purchasedMusic again
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO Transaction (user_email, music_id, amount, is_subscription) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, userEmail);
                stmt.setLong(2, music.getId());
                stmt.setDouble(3, 0.0);
                stmt.setBoolean(4, false);
                stmt.executeUpdate();
                musicManager.incrementDownloads(music.getId());
                userManager.saveUsers(); // Save to users.txt
                return true;
            } catch (SQLException e) {
                System.out.println("Error adding transaction: " + e.getMessage());
                return false;
            }
        }

        // Deduct balance and add music if not already purchased
        if (!user.getPurchasedMusic().contains(musicTitle) && user.getBalance() >= music.getPrice()) {
            boolean success = user.deductBalance(music.getPrice());
            if (success) {
                user.addPurchasedMusic(musicTitle);
                musicManager.incrementDownloads(music.getId());
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO Transaction (user_email, music_id, amount, is_subscription) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, userEmail);
                    stmt.setLong(2, music.getId());
                    stmt.setDouble(3, music.getPrice());
                    stmt.setBoolean(4, false);
                    stmt.executeUpdate();
                    userManager.saveUsers(); // Save to users.txt
                    return true;
                } catch (SQLException e) {
                    System.out.println("Error adding transaction: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    public boolean purchaseSubscription(String userEmail, double amount) {
        User user = userManager.getUserByEmail(userEmail);
        if (user == null) return false;

        userManager.activateSubscription(userEmail, 30);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Transaction (user_email, amount, is_subscription) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userEmail);
            stmt.setDouble(2, amount);
            stmt.setBoolean(3, true);
            stmt.executeUpdate();
            userManager.saveUsers(); // Save to users.txt
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding subscription: " + e.getMessage());
            return false;
        }
    }

    public List<Transaction> getTransactionsByUser(String userEmail) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Transaction WHERE user_email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userEmail);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getString("user_email"),
                        rs.getObject("music_id") != null ? rs.getLong("music_id") : null,
                        rs.getDouble("amount"),
                        rs.getBoolean("is_subscription")
                );
                transaction.setId(rs.getLong("id"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.out.println("Error getting transactions: " + e.getMessage());
        }
        return transactions;
    }

    public String downloadMusic(String userEmail, String musicTitle) {
        if (!userManager.canAccessMusic(userEmail, musicTitle)) {
            return null;
        }
        Music music = musicManager.getMusicByTitle(musicTitle);
        musicManager.incrementDownloads(music.getId());
        return music.getAudioBase64();
    }
}