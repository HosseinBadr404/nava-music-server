import java.time.LocalDateTime;

public class Transaction {
    private long id;
    private String userEmail;
    private Long musicId; 
    private double amount;
    private LocalDateTime timestamp;
    private boolean isSubscription;

    public Transaction(String userEmail, Long musicId, double amount, boolean isSubscription) {
        this.userEmail = userEmail;
        this.musicId = musicId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.isSubscription = isSubscription;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public Long getMusicId() { return musicId; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isSubscription() { return isSubscription; }
}