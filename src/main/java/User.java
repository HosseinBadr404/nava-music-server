import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String email;
    private final String passwordHash;
    private double balance;
    private List<String> purchasedMusic;
    private LocalDate subscriptionEndDate;

    public User(String name, String email, String passwordHash, double balance) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.balance = balance;
        this.purchasedMusic = new ArrayList<>();
        this.subscriptionEndDate = null;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public double getBalance() { return balance; }
    public List<String> getPurchasedMusic() { return purchasedMusic; }
    public LocalDate getSubscriptionEndDate() { return subscriptionEndDate; }

    public boolean isSubscriptionActive() {
        if (subscriptionEndDate == null) return false;
        return subscriptionEndDate.isAfter(LocalDate.now());
    }

    public int getRemainingSubscriptionDays() {
        if (!isSubscriptionActive()) return 0;
        return (int) (subscriptionEndDate.toEpochDay() - LocalDate.now().toEpochDay());
    }

    public boolean deductBalance(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public void activatePremiumSubscription(int days) {
        if (isSubscriptionActive()) {
            subscriptionEndDate = subscriptionEndDate.plusDays(days);
        } else {
            subscriptionEndDate = LocalDate.now().plusDays(days);
        }
    }

    public void activatePremiumSubscription(LocalDate endDate) {
        this.subscriptionEndDate = endDate;
    }

    public void addBalance(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    public void addPurchasedMusic(String musicTitle) {
        purchasedMusic.add(musicTitle);
    }

    public void updateProfile(String newName, String newEmail) {
        name = newName;
        email = newEmail;
    }
}
