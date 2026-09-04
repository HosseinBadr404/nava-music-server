import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserManager {
    private static final String USERS_FILE = System.getenv().getOrDefault(
            "NAVA_USERS_FILE", "data/users.txt");
    private List<User> users;
    private MusicManager musicManager;

    // Init & load users from file
    public UserManager() {
        users = new ArrayList<>();
        loadUsers();
    }

    // Set MusicManager reference
    public void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    // Load users from file
    private void loadUsers() {
        List<String> lines = FileUtil.readLines(USERS_FILE);
        for (String line : lines) {
            try {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    String name = parts[0];
                    String email = parts[1];
                    String passwordHash = parts[2];
                    double balance = Double.parseDouble(parts[3]);
                    User user = new User(name, email, passwordHash, balance);

                    if (parts.length > 4 && !parts[4].isEmpty()) {
                        String[] musicTitles = parts[4].split(",");
                        for (String title : musicTitles) {
                            user.addPurchasedMusic(title.trim());
                        }
                    }
                    if (parts.length > 5 && !parts[5].isEmpty()) {
                        user.activatePremiumSubscription(LocalDate.parse(parts[5]));
                    }
                    users.add(user);
                }
            } catch (Exception e) {
                System.out.println("Error parsing user line: " + line + ", error: " + e.getMessage());
            }
        }
    }

    // Save users to file
    public void saveUsers() {
        List<String> lines = users.stream().map(user -> {
            String purchasedMusic = String.join(",", user.getPurchasedMusic());
            String subscriptionEnd = user.getSubscriptionEndDate() != null ? user.getSubscriptionEndDate().toString() : "";
            return String.format("%s|%s|%s|%.2f|%s|%s",
                    user.getName(), user.getEmail(), user.getPasswordHash(),
                    user.getBalance(), purchasedMusic, subscriptionEnd);
        }).collect(Collectors.toList());
        FileUtil.writeLines(USERS_FILE, lines);
    }

    // Check user credentials
    public boolean login(String email, String password) {
        return users.stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email)
                        && PasswordHasher.verify(password, user.getPasswordHash()));
    }

    // Register new user
    public boolean signUp(String name, String email, String password) {
        if (name == null || name.isBlank() || email == null || email.isBlank()
                || password == null || password.length() < 8
                || users.stream().anyMatch(user -> user.getEmail().equalsIgnoreCase(email))) {
            return false;
        }
        users.add(new User(name.trim(), email.trim().toLowerCase(), PasswordHasher.hash(password), 10.0));
        saveUsers();
        return true;
    }

    // Find user by email
    public User getUserByEmail(String email) {
        return users.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // Add balance to user
    public boolean addBalance(String userEmail, double amount) {
        User user = getUserByEmail(userEmail);
        if (user == null || amount <= 0) return false;
        user.addBalance(amount);
        saveUsers();
        return true;
    }

    // Check if user exists
    public boolean userExists(String userEmail) {
        return users.stream().anyMatch(user -> user.getEmail().equals(userEmail));
    }

    // Check if user purchased specific music
    public boolean hasPurchased(String userEmail, String musicTitle) {
        User user = getUserByEmail(userEmail);
        if (user == null) return false;
        return user.getPurchasedMusic().contains(musicTitle);
    }

    // Check if user can access music
    public boolean canAccessMusic(String userEmail, String musicTitle) {
        User user = getUserByEmail(userEmail);
        if (user == null || musicManager == null) return false;
        Music music = musicManager.getMusicByTitle(musicTitle);
        if (music == null) return false;
        return music.isFree() || user.isSubscriptionActive() || hasPurchased(userEmail, musicTitle);
    }

    // Activate premium subscription
    public void activateSubscription(String userEmail, int days) {
        User user = getUserByEmail(userEmail);
        if (user != null) {
            user.activatePremiumSubscription(days);
            saveUsers();
        }
    }

    // Update user name/email
    public boolean updateUserInfo(String oldEmail, String newName, String newEmail) {
        User user = getUserByEmail(oldEmail);
        if (user == null) return false;

        if (!newEmail.equals(oldEmail) && users.stream().anyMatch(u -> u.getEmail().equals(newEmail))) {
            return false;
        }

        user.updateProfile(newName.trim(), newEmail.trim().toLowerCase());
        saveUsers();
        return true;
    }
}
