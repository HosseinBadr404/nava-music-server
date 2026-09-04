public class Comment {
    private long id;
    private String content;
    private int likes;
    private int dislikes;
    private String userEmail;
    private long musicId;

    public Comment(String content, String userEmail) {
        this.content = content;
        this.userEmail = userEmail;
        this.likes = 0;
        this.dislikes = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getDislikes() { return dislikes; }
    public void setDislikes(int dislikes) { this.dislikes = dislikes; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public long getMusicId() { return musicId; }
    public void setMusicId(long musicId) { this.musicId = musicId; }

    public void incrementLikes() { this.likes++; }
    public void incrementDislikes() { this.dislikes++; }
}