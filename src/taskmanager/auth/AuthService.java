package taskmanager.auth;

import taskmanager.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Manages users and authentication.
 * Uses SHA-256 password hashing.
 */
public class AuthService {

    private static AuthService instance;

    private final Map<String, User> users = new LinkedHashMap<>();
    private User currentUser;

    private AuthService() {
        // Default accounts
        registerUser("admin",     "admin123",   User.Role.ADMIN,     "Alice Admin");
        registerUser("dev1",      "dev123",     User.Role.DEVELOPER, "Bob Developer");
        registerUser("dev2",      "dev456",     User.Role.DEVELOPER, "Carol Coder");
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    public void registerUser(String username, String password, User.Role role, String displayName) {
        users.put(username.toLowerCase(), new User(username.toLowerCase(), hash(password), role, displayName));
    }

    /** Returns the logged-in user, or null on failure. */
    public User login(String username, String password) {
        User u = users.get(username.toLowerCase());
        if (u != null && u.getPasswordHash().equals(hash(password))) {
            currentUser = u;
            return u;
        }
        return null;
    }

    public void logout() { currentUser = null; }

    public User getCurrentUser() { return currentUser; }

    public boolean isLoggedIn() { return currentUser != null; }

    public List<User> getAllUsers() { return new ArrayList<>(users.values()); }

    public List<String> getUsernames() { return new ArrayList<>(users.keySet()); }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return input; // fallback
        }
    }
}
