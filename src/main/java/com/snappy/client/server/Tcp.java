package com.snappy.client.server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.snappy.client.ErrorManager;

public class Tcp {
    private final String host;
    private final int port;
    
    public Tcp(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(String snapshotPath) {
        System.out.println("Sending the snapshot...");
        sendSnapshot(snapshotPath);
    }

    private void sendSnapshot(String snapshotPath) {
        try {
            Socket socket = new Socket(host, port);
            List<String> folders = getFolderList(snapshotPath);
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            List<File> files = processFiles(snapshotPath);

            sendFolderList(folders, socket, outputStream);
            sendFiles(files, outputStream);

            outputStream.close();
        } catch (IOException e) {
            ErrorManager.exitWithError("Unknown host. Be sure to have typed the correct informations during installation", e);
        }
    }

    private List<String> getFolderList(String path) {
        List<String> folders = new ArrayList<>();
        File directory = new File(path);

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    folders.add(file.getPath());
                    folders.addAll(getFolderList(file.getPath()));
                }
            }
        }

        return folders;
    }

    private void sendFolderList(List<String> folders, Socket socket, ObjectOutputStream outputStream) {
        try {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("type", ConnectionType.POST);
            data.put("content_type", ConnectionType.FOLDER_LIST);
            data.put("content", folders);

            outputStream.writeObject(data);
        } catch (IOException e) {
            ErrorManager.exitWithError("Something went wrong while sending the folder list. Report this error to the developer.", e);
        }
    }

    private void sendFiles(List<File> files, ObjectOutputStream outputStream) {
        try {
            Map<String, Object> content = new LinkedHashMap<String, Object>();

            for (File file : files) {
                content.put("name", file.getName());
                content.put("path", file.getPath());
                content.put("content", Files.readAllBytes(file.toPath()));
            }

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("type", ConnectionType.POST);
            data.put("content_type", ConnectionType.FILE_MAP);
            data.put("content", content);

            outputStream.writeObject(data);
        } catch (IOException e) {
            ErrorManager.exitWithError("Something went wrong while sending the file. Report this error to the developer.", e);
        }
    }

    private List<File> processFiles(String path) {
        List<File> fileList = new ArrayList<>();
        File directory = new File(path);

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList.addAll(processFiles(file.getPath()));
                } else {
                    fileList.add(file);
                }
            }
        }

        return fileList;
    }


    private enum ConnectionType {
        POST,
        FOLDER_LIST,
        FILE_MAP,
    }
}
