package services;

import database.UserDAO;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    // Sign-up logic
    public boolean registerUser(String username, String password) {
        if (userDAO.registerUser(username, password)) {
            System.out.println("User registered successfully!");
            return true;
        } else {
            System.out.println("Failed to register user. The username might already exist.");
            return false;
        }
    }

    // Login logic
    public boolean loginUser(String username, String password) {
        if (userDAO.validateLogin(username, password)) {
            System.out.println(username + " logged in successfully!");
            return true;
        } else {
            System.out.println("Invalid username or password. Please try again.");
            return false;
        }
    }

    // Logout logic
    public void logoutUser(String username) {
        // Logout is a client-side action in this basic implementation.
        System.out.println(username + " logged out successfully!");
    }
}
