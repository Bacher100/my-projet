package com.conference.util;

import com.conference.model.User;

/**
 * Gestionnaire de session utilisateur (Singleton).
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser()          { return currentUser; }
    public boolean isLoggedIn()           { return currentUser != null; }
    public void logout()                  { currentUser = null; }

    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equals(currentUser.getRole());
    }
    public boolean isResponsable() {
        return isLoggedIn() &&
                ("ADMIN".equals(currentUser.getRole()) ||
                        "RESPONSABLE".equals(currentUser.getRole()));
    }
}