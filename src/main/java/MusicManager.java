import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MusicManager {
    private UserManager userManager;

    public MusicManager() {

    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public List<Music> getMusicByCategory(String category) {
        List<Music> musicList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Music WHERE category = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Music music = new Music();
                music.setId(rs.getLong("id"));
                music.setTitle(rs.getString("title"));
                music.setArtist(rs.getString("artist"));
                music.setImage(rs.getString("image"));
                music.setPrice(rs.getDouble("price"));
                music.setDownloads(rs.getInt("downloads"));
                music.setRating(rs.getDouble("rating"));
                music.setCategory(rs.getString("category"));
                music.setAudioBase64(rs.getString("audio_base64"));
                musicList.add(music);
            }
        } catch (SQLException e) {
            System.out.println("Error getting music by category: " + e.getMessage());
        }
        return musicList;
    }

    public Music getMusicByTitle(String title) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Music WHERE title = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Music music = new Music();
                music.setId(rs.getLong("id"));
                music.setTitle(rs.getString("title"));
                music.setArtist(rs.getString("artist"));
                music.setImage(rs.getString("image"));
                music.setPrice(rs.getDouble("price"));
                music.setDownloads(rs.getInt("downloads"));
                music.setRating(rs.getDouble("rating"));
                music.setCategory(rs.getString("category"));
                return music;
            }
        } catch (SQLException e) {
            System.out.println("Error getting music by title: " + e.getMessage());
        }
        return null;
    }

    public void incrementDownloads(long musicId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Music SET downloads = downloads + 1 WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, musicId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error incrementing downloads: " + e.getMessage());
        }
    }

    /*public String downloadMusic(String userEmail, String musicTitle) {

        if (userManager == null) {
            System.out.println("UserManager is not initialized");
            return null;
        }


        User user = userManager.getUserByEmail(userEmail);
        if (user == null) {
            System.out.println("User not found: " + userEmail);
            return null;
        }


        Music music = getMusicByTitle(musicTitle);
        if (music == null) {
            System.out.println("Music not found: " + musicTitle);
            return null;
        }


        boolean hasAccess = userManager.hasPurchased(userEmail, musicTitle) || user.isSubscriptionActive();
        if (!hasAccess) {
            System.out.println("User " + userEmail + " has no access to music: " + musicTitle);
            return null;
        }


        incrementDownloads(music.getId());


        String audioBase64 = music.getAudioBase64();
        if (audioBase64 == null || audioBase64.isEmpty()) {
            System.out.println("No audio data available for music: " + musicTitle);
            return null;
        }
        return audioBase64;
    }*/
}