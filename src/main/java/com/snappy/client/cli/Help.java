package com.snappy.client.cli;

public class Help {
    
    Help() {}

    public void print() {
        System.out.println("Usage: snappy [option] [<params>]\n");

        System.out.println("Options:");
        System.out.println("\t-t\t\tTakes a snapshot of the system.");
        System.out.println("\t-l\t\tLists all snapshots saved in the server.");
        System.out.println("\t-r <snapshot>\tRestores the system to the specified snapshot.\n");

        System.out.println("\t-h\t\tPrints this help message.");
    }
}
