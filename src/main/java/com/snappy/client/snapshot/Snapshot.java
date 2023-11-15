package com.snappy.client.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.snappy.client.ErrorManager;
import com.snappy.client.server.Tcp;


/*
 * This class is responsible for the creation of the snapshot.
 * It can take a snapshot of the subvolume specified in the config file.
 * It will also manage the snapshot list received from the server and restore the system to a previous state (hopefully).
 */
public class Snapshot {
    
    public Snapshot() {}

    /*
     * This method is responsible for the creation of the snapshot.
     * 
     * Called by:
     * - CommandLine.parse()
     */
    public void take() {
        System.out.println("Taking snapshot...");
        takeSnapshot();
    }
    
    /*
     * This method manages all the methods to create a snapshot and sends it to the server.
     * 
     * Called by:
     * - take()
     */
    private void takeSnapshot() {
        String subvolume = (String) readConfig(Config.SUBVOLUME);
        String snapshotName = "snappy-" + LocalDate.now().toString();

        createSubvolume(subvolume);
        emptySnapshotDirectory();
        createSnapshot(subvolume, snapshotName);
        sendSnapshot((String) readConfig(Config.IP), (int) readConfig(Config.PORT), "/etc/snappy/snapshots/" + snapshotName);
    }

    /*
     * This method creates the subvolume where the snapshot will be taken.
     * 
     * Called by:
     * - takeSnapshot()
     */
    private void createSubvolume(String subvolume) {
        ProcessBuilder processBuilder = new ProcessBuilder("btrfs",
                                                           "subvolume",
                                                           "create",
                                                           subvolume,
                                                           "/etc/snappy/snapshots/snappy-" + LocalDate.now().toString());

        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            ErrorManager.exitWithError("Something went wrong while creating the subvolume. Report this error to the developer.", e);
        }
    }

    /*
     * This method gets the config data and returns the value of the specified config.
     * 
     * Called by:
     * - takeSnapshot()
     */
    private Object readConfig(Config config) {
        Map<String, Object> data = readYaml("/etc/snappy/config.yml");
        switch (config) {
            case SUBVOLUME:
                return data.get("default_subvolume");
            case IP:
                return data.get("server_ip");
            case PORT:
                return data.get("server_port");
            default:
                return null;
        }
    }

    /*
     * This method reads the config file and returns a map of the data.
     * 
     * Called by:
     * - readConfig()
     */
    private Map<String, Object> readYaml(String filePath) {
        Yaml yaml = new Yaml();
        try (InputStream stream = Files.newInputStream(Paths.get(filePath))) {
            return yaml.load(stream);
        } catch (IOException e) {
            ErrorManager.exitWithError("Something went wrong while reading the config file. Report this error to the developer.", e);
            return null;
        }
    }

    /*
     * This method empties the snapshot directory.
     * 
     * Called by:
     * - takeSnapshot()
     */
    private void emptySnapshotDirectory() {
        String path = "/etc/snappy/snapshots";

        try {
            try (var directoryStream = Files.newDirectoryStream(Paths.get(path))) {
                for (var file : directoryStream) {
                    Files.delete(file);
                }
            }
        } catch (IOException e) {
            ErrorManager.exitWithError("Something went wrong while emptying the snapshot directory. Report this error to the developer.", e);
        }
    }

    /*
     * This method creates the snapshot and saves it in /etc/snappy/snapshots.
     * 
     * Called by:
     * - takeSnapshot()
     */
    private void createSnapshot(String subvolume, String snapshotName) {
        ProcessBuilder processBuilder = new ProcessBuilder("btrfs",
                                                           "subvolume",
                                                           "snapshot",
                                                           subvolume,
                                                           "/etc/snappy/snapshots/" + snapshotName);

        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            ErrorManager.exitWithError("Something went wrong while taking the snapshot. Report this error to the developer.", e);
        }
    }

    /*
     * This method sends the snapshot to the server.
     * 
     * Called by:
     * - takeSnapshot()
     */
    private void sendSnapshot(String host, int port, String snapshotPath) {
        Tcp tcp = new Tcp(host, port);
        tcp.send(snapshotPath);
    }


    /*
     * This enum is responsible for the config values.
     * 
     * Called by:
     * - readConfig()
     */
    private enum Config {
        SUBVOLUME,
        IP,
        PORT
    }
}
