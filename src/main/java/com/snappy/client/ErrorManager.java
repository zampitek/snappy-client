package com.snappy.client;

public class ErrorManager {
    
    public static void exitWithError(String message, Throwable e) {
        if (App.DEBUG_MODE) {
            e.printStackTrace();
            System.exit(1);
        } else {
            System.out.println(message);
            System.exit(1);
        }
    }
}
