package com.mindshare.auth.model;
//Holds the currently logged-in user's auth token ans basic info in memory.

public class UserSession {

    private static String token;
    private static String userName;
    private static String userRole;

    public static void set(String token, String userName, String userRole) {
        UserSession.token = token;
        UserSession.userName = userName;
        UserSession.userRole = userRole;
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
    public static boolean isLoggedIn() {
        return token != null;
    }

    public static void clear() {
        token = null;
        userName = null;
        userRole = null;
    }
}


