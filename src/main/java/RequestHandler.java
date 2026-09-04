import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;

public class RequestHandler {
    private final UserManager userManager;
    private final MusicManager musicManager;
    private final CommentManager commentManager;
    private final TransactionManager transactionManager;
    private final Gson gson = new Gson();

    // Init managers and link dependencies
    public RequestHandler() {
        userManager = new UserManager();
        musicManager = new MusicManager();
        musicManager.setUserManager(userManager);
        userManager.setMusicManager(musicManager);
        commentManager = new CommentManager(userManager);
        transactionManager = new TransactionManager(userManager, musicManager);
    }

    // Process incoming JSON request and return JSON response
    public String handleRequest(String message) {
        try {
            JsonObject request = gson.fromJson(message, JsonObject.class);
            String action = request.get("action").getAsString();
            String requestId = request.has("requestId") ? request.get("requestId").getAsString() : "";
            JsonObject data = request.has("data") ? request.getAsJsonObject("data") : new JsonObject();

            JsonObject response = new JsonObject();
            response.addProperty("action", action);
            response.addProperty("requestId", requestId);

            switch (action) {
                case "login":
                    String email = data.get("email").getAsString();
                    String password = data.get("password").getAsString();
                    boolean loginSuccess = userManager.login(email, password);
                    response.addProperty("status", loginSuccess ? "success" : "error");
                    response.addProperty("message", loginSuccess ? "Login OK" : "Wrong email or password");
                    if (loginSuccess) {
                        User user = userManager.getUserByEmail(email);
                        if (user != null) {
                            JsonObject userData = new JsonObject();
                            userData.addProperty("name", user.getName());
                            userData.addProperty("email", user.getEmail());
                            userData.addProperty("balance", user.getBalance());
                            userData.add("purchasedMusic", gson.toJsonTree(user.getPurchasedMusic()));
                            if (user.getSubscriptionEndDate() != null) {
                                userData.addProperty("subscriptionEndDate", user.getSubscriptionEndDate().toString());
                            }
                            response.add("data", userData);
                        } else {
                            response.addProperty("status", "error");
                            response.addProperty("message", "User not found");
                        }
                    }
                    break;

                case "signUp":
                    String name = data.get("name").getAsString();
                    String signupEmail = data.get("email").getAsString();
                    String signupPassword = data.get("password").getAsString();
                    boolean signupSuccess = userManager.signUp(name, signupEmail, signupPassword);
                    response.addProperty("status", signupSuccess ? "success" : "error");
                    response.addProperty("message", signupSuccess ? "Sign-up OK" : "Email already exists");
                    if (signupSuccess) {
                        User user = userManager.getUserByEmail(signupEmail);
                        JsonObject userData = new JsonObject();
                        userData.addProperty("name", user.getName());
                        userData.addProperty("email", user.getEmail());
                        userData.addProperty("balance", user.getBalance());
                        userData.add("purchasedMusic", gson.toJsonTree(user.getPurchasedMusic()));
                        response.add("data", userData);
                    }
                    break;

                case "get_user_info":
                    String userEmail = data.get("userEmail").getAsString();
                    User user = userManager.getUserByEmail(userEmail);
                    if (user != null) {
                        JsonObject userData = new JsonObject();
                        userData.addProperty("name", user.getName());
                        userData.addProperty("email", user.getEmail());
                        userData.addProperty("balance", user.getBalance());
                        userData.add("purchasedMusic", gson.toJsonTree(user.getPurchasedMusic()));
                        if (user.getSubscriptionEndDate() != null) {
                            userData.addProperty("subscriptionEndDate", user.getSubscriptionEndDate().toString());
                        }
                        response.addProperty("status", "success");
                        response.add("data", userData);
                    } else {
                        response.addProperty("status", "error");
                        response.addProperty("message", "User not found");
                    }
                    break;

                case "update_user_info":
                    String oldEmail = data.get("oldEmail").getAsString();
                    String newName = data.get("newName").getAsString();
                    String newEmail = data.get("newEmail").getAsString();
                    boolean updateSuccess = userManager.updateUserInfo(oldEmail, newName, newEmail);
                    response.addProperty("status", updateSuccess ? "success" : "error");
                    response.addProperty("message", updateSuccess ? "User info updated" : "Failed to update user info");
                    if (updateSuccess) {
                        User updatedUser = userManager.getUserByEmail(newEmail);
                        if (updatedUser != null) {
                            JsonObject userData = new JsonObject();
                            userData.addProperty("name", updatedUser.getName());
                            userData.addProperty("email", updatedUser.getEmail());
                            userData.addProperty("balance", updatedUser.getBalance());
                            userData.add("purchasedMusic", gson.toJsonTree(updatedUser.getPurchasedMusic()));
                            if (updatedUser.getSubscriptionEndDate() != null) {
                                userData.addProperty("subscriptionEndDate", updatedUser.getSubscriptionEndDate().toString());
                            }
                            response.add("data", userData);
                        }
                    }
                    break;

                case "addBalance":
                    userEmail = data.get("userEmail").getAsString();
                    double amount = data.get("amount").getAsDouble();
                    boolean balanceSuccess = userManager.addBalance(userEmail, amount);
                    if (balanceSuccess) {
                        user = userManager.getUserByEmail(userEmail);
                        JsonObject userData = new JsonObject();
                        userData.addProperty("name", user.getName());
                        userData.addProperty("email", user.getEmail());
                        userData.addProperty("balance", user.getBalance());
                        userData.add("purchasedMusic", gson.toJsonTree(user.getPurchasedMusic()));
                        if (user.getSubscriptionEndDate() != null) {
                            userData.addProperty("subscriptionEndDate", user.getSubscriptionEndDate().toString());
                        }
                        response.addProperty("status", "success");
                        response.add("data", userData);
                    } else {
                        response.addProperty("status", "error");
                        response.addProperty("message", "Failed to add balance");
                    }
                    break;

                case "purchase_music":
                    userEmail = data.get("userEmail").getAsString();
                    String musicTitle = data.get("musicTitle").getAsString();
                    boolean purchaseSuccess = transactionManager.purchaseMusic(userEmail, musicTitle);
                    response.addProperty("status", purchaseSuccess ? "success" : "error");
                    response.addProperty("message", purchaseSuccess ? "Purchase successful" : "Purchase failed");
                    break;

                case "activate_subscription":
                    userEmail = data.get("userEmail").getAsString();
                    amount = data.get("amount").getAsDouble();
                    boolean subSuccess = transactionManager.purchaseSubscription(userEmail, amount);
                    response.addProperty("status", subSuccess ? "success" : "error");
                    response.addProperty("message", subSuccess ? "Subscription activated" : "Subscription failed");
                    if (subSuccess) {
                        user = userManager.getUserByEmail(userEmail);
                        JsonObject subData = new JsonObject();
                        subData.addProperty("isActive", user.isSubscriptionActive());
                        subData.addProperty("remainingDays", user.getRemainingSubscriptionDays());
                        response.add("data", subData);
                    }
                    break;

                case "check_subscription":
                    userEmail = data.get("userEmail").getAsString();
                    user = userManager.getUserByEmail(userEmail);
                    if (user != null) {
                        JsonObject subData = new JsonObject();
                        subData.addProperty("isActive", user.isSubscriptionActive());
                        subData.addProperty("remainingDays", user.getRemainingSubscriptionDays());
                        response.addProperty("status", "success");
                        response.add("data", subData);
                    } else {
                        response.addProperty("status", "error");
                        response.addProperty("message", "User not found");
                    }
                    break;

                case "check_purchase":
                    userEmail = data.get("userEmail").getAsString();
                    String musicTitle2 = data.get("musicTitle").getAsString();
                    boolean hasPurchased = userManager.hasPurchased(userEmail, musicTitle2);
                    JsonObject purchaseData = new JsonObject();
                    purchaseData.addProperty("hasPurchased", hasPurchased);
                    response.addProperty("status", "success");
                    response.add("data", purchaseData);
                    break;

                case "logout":
                    userEmail = data.get("userEmail").getAsString();
                    boolean logoutSuccess = userManager.userExists(userEmail);
                    response.addProperty("status", logoutSuccess ? "success" : "error");
                    response.addProperty("message", logoutSuccess ? "Logout OK" : "User not found");
                    break;

                case "addComment":
                    String content = data.get("content").getAsString();
                    userEmail = data.get("userEmail").getAsString();
                    long musicId = data.get("musicId").getAsLong();
                    boolean commentSuccess = commentManager.addComment(content, userEmail, musicId);
                    response.addProperty("status", commentSuccess ? "success" : "error");
                    response.addProperty("message", commentSuccess ? "Comment added" : "Failed to add comment");
                    break;

                case "getCommentsByMusic":
                    long commentsMusicId = data.get("musicId").getAsLong();
                    List<Comment> comments = commentManager.getCommentsByMusic(commentsMusicId);
                    response.addProperty("status", "success");
                    response.add("data", gson.toJsonTree(comments));
                    break;

                case "reactToComment":
                    long commentId = data.get("commentId").getAsLong();
                    userEmail = data.get("userEmail").getAsString();
                    String reaction = data.get("reaction").getAsString();
                    boolean reactSuccess = commentManager.reactToComment(commentId, userEmail, reaction);
                    response.addProperty("status", reactSuccess ? "success" : "error");
                    response.addProperty("message", reactSuccess ? "Reaction updated" : "Failed to update reaction");
                    break;

                case "downloadMusic":
                    userEmail = data.get("userEmail").getAsString();
                    String musicTitle3 = data.get("musicTitle").getAsString();
                    String base64Audio = transactionManager.downloadMusic(userEmail, musicTitle3);
                    response.addProperty("status", base64Audio != null ? "success" : "error");
                    response.addProperty("message", base64Audio != null ? "Download OK" : "Download failed");
                    if (base64Audio != null) {
                        JsonObject dataResponse = new JsonObject();
                        dataResponse.addProperty("audioBase64", base64Audio);
                        response.add("data", dataResponse);
                    }
                    break;

                case "getMusicByCategory":
                    String category = data.get("category").getAsString();
                    List<Music> musicList = musicManager.getMusicByCategory(category);
                    response.addProperty("status", "success");
                    response.add("data", gson.toJsonTree(musicList));
                    break;

                case "getMusicByTitle":
                    String title = data.get("title").getAsString();
                    Music music = musicManager.getMusicByTitle(title);
                    response.addProperty("status", "success");
                    response.add("data", gson.toJsonTree(music));
                    break;

                default:
                    response.addProperty("status", "error");
                    response.addProperty("message", "Unknown action: " + action);
                    break;
            }

            String responseJson = gson.toJson(response);
            System.out.println("Sending response length: " + responseJson.length());
            return responseJson;
        } catch (JsonSyntaxException e) {
            System.out.println("JSON parse error: " + e.getMessage());
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Bad JSON format: " + e.getMessage());
            return gson.toJson(errorResponse);
        } catch (Exception e) {
            System.out.println("RequestHandler error: " + e.getMessage());
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Server error: " + e.getMessage());
            return gson.toJson(errorResponse);
        }
    }
}
