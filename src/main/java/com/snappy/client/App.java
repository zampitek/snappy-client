package com.snappy.client;

import java.io.IOException;
import java.util.Scanner;

import com.snappy.client.cli.CommandLine;
import com.snappy.client.install.Install;

public class App {
    public static boolean DEBUG_MODE;


    // This method is the entry point of the application, it checks the installation and parses the command line arguments.
    public static void main( String[] args ) throws IOException {
        checkInstallation();

        CommandLine commandLine = new CommandLine(args);
        commandLine.parse();
    }

    // This method checks if the application is installed and prompts the user to install it if it's not.
    private static void checkInstallation() {
        if (!Install.isInstalled()) {
            System.out.println("Snappy is not installed. Do you want to install it? [Y/n]");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();
            scanner.close();

            if (answer.equals("Y") || answer.equals("y")) {
                Install install = new Install();
                install.install();
            } else {
                System.exit(0);
            }
        }
    }
}
