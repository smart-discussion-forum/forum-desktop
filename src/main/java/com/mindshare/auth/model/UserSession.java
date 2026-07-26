package com.mindshare.auth.model;
//Holds the currently logged-in user's auth token and basic info in memory.

public class UserSession {

    private static String token;
    private static String userName;
    private static String userRole;
    private static String userEmail;
    private static int userId = -1;

    public static void set(String token, String userName, String userRole, String userEmail, int userId) {
        UserSession.token = token;
        UserSession.userName = userName;
        UserSession.userRole = userRole;
        UserSession.userEmail = userEmail;
        UserSession.userId = userId;

    }

    public static String getToken() {
        return token;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getUserRole() {
        return userRole;
    }

    public static String getUserEmail() {
        return userEmail;
    }

    public static int getUserId() {
        return userId;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }

    public static void clear() {
        token = null;
        userName = null;
        userRole = null;
        userEmail = null;
        userId = -1;
    }
}


