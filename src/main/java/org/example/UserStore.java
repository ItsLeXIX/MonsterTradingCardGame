package org.example;

import java.util.HashMap;
import java.util.Map;

public class UserStore {
    private static final Map<String, User> users = new HashMap<>();
    public static boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, new User(username, password));
        return true;
    }
    public static boolean authenticateUser(String username, String password) {
        User user = users.get(username);
        return user != null && user.getPassword().equals(password);
    }
}