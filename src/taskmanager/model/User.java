package taskmanager.model;

import java.io.Serializable;

/**
 * Represents an application user with a role (ADMIN or DEVELOPER).
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Role { ADMIN, DEVELOPER }

    private final String username;
    private final String passwordHash; // SHA-256 hex
    private final Role   role;
    private final String displayName;
    private final String avatarInitials;

    public User(String username, String passwordHash, Role role, String displayName) {
        this.username       = username;
        this.passwordHash   = passwordHash;
        this.role           = role;
        this.displayName    = displayName;
        // derive initials
        String[] parts = displayName.split(" ");
        if (parts.length >= 2) avatarInitials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        else                   avatarInitials = displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
    }

    public String getUsername()       { return username; }
    public String getPasswordHash()   { return passwordHash; }
    public Role   getRole()           { return role; }
    public String getDisplayName()    { return displayName; }
    public String getAvatarInitials() { return avatarInitials; }

    public boolean isAdmin()     { return role == Role.ADMIN; }
    public boolean isDeveloper() { return role == Role.DEVELOPER; }

    @Override public String toString() { return displayName + " (" + role + ")"; }
}
