import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.HashMap;
import java.util.Map;

public class CommentManager {
    private final UserManager userManager;
    private final Gson gson = new Gson();

    public CommentManager(UserManager userManager) {
        this.userManager = userManager;
    }

    // Add a new comment for a music track
    public boolean addComment(String content, String userEmail, long musicId) {
        if (!userManager.userExists(userEmail) || content == null || content.trim().isEmpty()) {
            System.out.println("Add comment failed: Invalid user or empty content");
            return false;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Comment (content, user_email, music_id, likes, dislikes, liked_by, disliked_by) VALUES (?, ?, ?, 0, 0, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, content);
            stmt.setString(2, userEmail);
            stmt.setLong(3, musicId);
            stmt.setString(4, "[]");
            stmt.setString(5, "[]");
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding comment: " + e.getMessage());
            return false;
        }
    }

    // Get all comments for a given music ID
    public List<Comment> getCommentsByMusic(long musicId) {
        List<Comment> comments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, content, likes, dislikes, user_email, music_id FROM Comment WHERE music_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, musicId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment(rs.getString("content"), rs.getString("user_email"));
                comment.setId(rs.getLong("id"));
                comment.setLikes(rs.getInt("likes"));
                comment.setDislikes(rs.getInt("dislikes"));
                comment.setMusicId(rs.getLong("music_id"));
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.out.println("Error getting comments: " + e.getMessage());
        }
        return comments;
    }

    // Get the current reactions (like/dislike) of a user for a list of comment IDs
    public Map<Long, String> getUserReactions(List<Long> commentIds, String userEmail) {
        Map<Long, String> reactions = new HashMap<>();
        if (commentIds.isEmpty()) return reactions;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String placeholders = String.join(",", commentIds.stream().map(id -> "?").toArray(String[]::new));
            String sql = "SELECT id, liked_by, disliked_by FROM Comment WHERE id IN (" + placeholders + ")";
            PreparedStatement stmt = conn.prepareStatement(sql);
            int index = 1;
            for (Long id : commentIds) stmt.setLong(index++, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                long commentId = rs.getLong("id");
                List<String> likedBy = gson.fromJson(rs.getString("liked_by"), new TypeToken<List<String>>(){}.getType());
                List<String> dislikedBy = gson.fromJson(rs.getString("disliked_by"), new TypeToken<List<String>>(){}.getType());

                if (likedBy != null && likedBy.contains(userEmail)) {
                    reactions.put(commentId, "like");
                } else if (dislikedBy != null && dislikedBy.contains(userEmail)) {
                    reactions.put(commentId, "dislike");
                } else {
                    reactions.put(commentId, null);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting user reactions: " + e.getMessage());
        }
        return reactions;
    }

    // Apply or remove a user reaction (like/dislike) for a specific comment
    public boolean reactToComment(long commentId, String userEmail, String reaction) {
        if (!userManager.userExists(userEmail) || (!reaction.equals("like") && !reaction.equals("dislike"))) {
            System.out.println("Invalid user or reaction: " + userEmail + ", " + reaction);
            return false;
        }
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String selectSql = "SELECT liked_by, disliked_by, likes, dislikes FROM Comment WHERE id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setLong(1, commentId);
            ResultSet rs = selectStmt.executeQuery();
            if (!rs.next()) {
                conn.rollback();
                return false;
            }

            List<String> likedBy = gson.fromJson(rs.getString("liked_by"), new TypeToken<List<String>>(){}.getType());
            List<String> dislikedBy = gson.fromJson(rs.getString("disliked_by"), new TypeToken<List<String>>(){}.getType());
            if (likedBy == null) likedBy = new ArrayList<>();
            if (dislikedBy == null) dislikedBy = new ArrayList<>();

            int likes = rs.getInt("likes");
            int dislikes = rs.getInt("dislikes");

            boolean inLiked = likedBy.contains(userEmail);
            boolean inDisliked = dislikedBy.contains(userEmail);

            if (reaction.equals("like")) {
                if (inLiked) { likedBy.remove(userEmail); likes--; }
                else { if (inDisliked) { dislikedBy.remove(userEmail); dislikes--; } likedBy.add(userEmail); likes++; }
            } else {
                if (inDisliked) { dislikedBy.remove(userEmail); dislikes--; }
                else { if (inLiked) { likedBy.remove(userEmail); likes--; } dislikedBy.add(userEmail); dislikes++; }
            }

            String updateSql = "UPDATE Comment SET liked_by = ?, disliked_by = ?, likes = ?, dislikes = ? WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, gson.toJson(likedBy));
            updateStmt.setString(2, gson.toJson(dislikedBy));
            updateStmt.setInt(3, likes);
            updateStmt.setInt(4, dislikes);
            updateStmt.setLong(5, commentId);
            if (updateStmt.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("Error reacting to comment: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException ignored) {}
        }
    }
}
