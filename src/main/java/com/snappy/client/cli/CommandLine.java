package com.snappy.client.cli;

import com.snappy.client.App;
import com.snappy.client.install.Install;

import java.util.List;

public class CommandLine {
    private String[] args;
    
    public CommandLine(String[] args) {
        this.args = args;
    }

    public void parse() {
        if (args.length == 0) {
            Help help = new Help();
            help.print();
        } else {
            for (String arg : args) {
                if (arg.equals("--debug")) {
                    enableDebugMode();

                    List<String> argsList = List.of(args);
                    argsList.remove("--debug");
                    args = argsList.toArray(new String[argsList.size()]);
                }
            }

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

    private void enableDebugMode() {
        App.DEBUG_MODE = true;
    }



    public enum Arg {
        TAKE_SNAPSHOT,
        LIST_SNAPSHOTS,
        RESTORE_SNAPSHOT,
        HELP,
        UNINSTALL
    }
}
