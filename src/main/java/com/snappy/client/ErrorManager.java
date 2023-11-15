package com.snappy.client;

/*
 * This class is responsible for the management of the errors.
 * It can print the stack trace of an exception if the debug mode is enabled.
 * It can also print a custom message and exit the application.
 */
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
