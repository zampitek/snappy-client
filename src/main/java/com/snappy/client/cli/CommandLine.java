package com.snappy.client.cli;

import com.snappy.client.App;
import com.snappy.client.install.Install;

import java.util.List;

/*
 * This class is responsible for parsing the command line arguments.
 * It manages the commands for taking, listing and restoring snapshots.
 * It also manages the help command and the uninstall command.
 */

public class CommandLine {
    private String[] args;
    
    // This constructor is responsible for parsing the command line arguments.
    public CommandLine(String[] args) {
        this.args = args;
    }

    /*
     * This method is responsible for parsing the command line arguments.
     * 
     * Called by:
     * - App.main()
     */
    public void parse() {
        // If no arguments are passed, the help command is executed.
        if (args.length == 0) {
            Help help = new Help();
            help.print();
        } else {
            // If the debug flag is passed, the debug mode is enabled and the flag is removed from the arguments.
            for (String arg : args) {
                if (arg.equals("--debug")) {
                    enableDebugMode();

                    List<String> argsList = List.of(args);
                    argsList.remove("--debug");
                    args = argsList.toArray(new String[argsList.size()]);
                }
            }

            // The first argument is parsed and the corresponding command is executed.
            Arg arg = getArg(args[0]);
            if (arg == null) {
                System.out.println("Invalid option: " + args[0]);
                System.out.println("Try 'snappy -h' for more information.");
            } else {
                switch (arg) {
                    case TAKE_SNAPSHOT:
                        System.out.println("Taking snapshot...");
                        break;
                    case LIST_SNAPSHOTS:
                        System.out.println("Listing snapshots...");
                        break;
                    case RESTORE_SNAPSHOT:
                        System.out.println("Restoring snapshot...");
                        break;
                    case HELP:
                        Help help = new Help();
                        help.print();
                        break;
                    case UNINSTALL:
                        System.out.println("Uninstalling Snappy...");
                        Install install = new Install();
                        install.uninstall();
                        System.exit(0);
                        break;
                }
            }
        }
    }

    /*
     * This method is responsible for parsing the first argument.
     * 
     * Called by:
     * - parse()
     */
    private Arg getArg(String arg) {
        switch (arg) {
            case "-t":
                return Arg.TAKE_SNAPSHOT;
            case "-l":
                return Arg.LIST_SNAPSHOTS;
            case "-r":
                return Arg.RESTORE_SNAPSHOT;
            case "-h":
                return Arg.HELP;
            case "-u":
                return Arg.UNINSTALL;
            default:
                return null;
        }
    }

    /*
     * This method is responsible for enabling the debug mode.
     * 
     * Called by:
     * - parse()
     */
    private void enableDebugMode() {
        App.DEBUG_MODE = true;
    }


    /*
     * This enum is responsible for storing the possible arguments.
     * 
     * Called by:
     * - getArg()
     * - parse()
     */
    public enum Arg {
        TAKE_SNAPSHOT,
        LIST_SNAPSHOTS,
        RESTORE_SNAPSHOT,
        HELP,
        UNINSTALL
    }
}
