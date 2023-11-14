package com.snappy.client.snapshot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.snappy.client.ErrorManager;

public class Snapshot {
    
    public Snapshot() {}

    public void take() {
        System.out.println("Taking snapshot...");
        takeSnapshot();
    }

    private void takeSnapshot() {
        String subvolume = (String) readConfig(Config.SUBVOLUME);
        String snapshotName = "snappy-" + LocalDate.now().toString();

        createSubvolume(subvolume);
        emptySnapshotDirectory();
        createSnapshot(subvolume, snapshotName);
    }

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

    private Map<String, Object> readYaml(String filePath) {
        Yaml yaml = new Yaml();
        try (InputStream stream = Files.newInputStream(Paths.get(filePath))) {
            return yaml.load(stream);
        } catch (IOException e) {
            ErrorManager.exitWithError("Something went wrong while reading the config file. Report this error to the developer.", e);
            return null;
        }
    }

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



    private enum Config {
        SUBVOLUME,
        IP,
        PORT
    }
}
