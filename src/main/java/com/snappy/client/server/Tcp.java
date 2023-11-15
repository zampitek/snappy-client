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

/*
 * This class is responsible for the communication with the server.
 * It can send the snapshot to the server (for now).
 */
public class Tcp {
    private final String host;
    private final int port;
    
    /*
     * This constructor is responsible for the creation of the Tcp object.
     * 
     * Called by:
     * - Snapshot.sendSnapshot()
     */
    public Tcp(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /*
     * This method is responsible for the sending of the snapshot to the server.
     * 
     * Called by:
     * - Snapshot.sendSnapshot()
     */
    public void send(String snapshotPath) {
        System.out.println("Sending the snapshot...");
        sendSnapshot(snapshotPath);
    }

    /*
     * This method is responsible for the sending of the snapshot to the server.
     * 
     * Called by:
     * - send()
     */
    private void sendSnapshot(String snapshotPath) {
        try {
            // The following methods are responsible for the creation of the socket and the sending of the snapshot.
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

    /*
     * This method is responsible for the creation of the list of folders to send to the server.
     * 
     * Called by:
     * - sendSnapshot()
     */
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
    
    /*
     * This method is responsible for the sending of the list of folders to the server.
     * 
     * Called by:
     * - sendSnapshot()
     */
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

    /*
     * This method is responsible for the sending of the files to the server.
     * 
     * Called by:
     * - sendSnapshot()
     */
    private void sendFiles(List<File> files, ObjectOutputStream outputStream) {
        try {
            // The following methods are responsible for the creation of the map of files to send to the server.
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
    
    /*
     * This method is responsible for the creation of the list of files to send to the server.
     * 
     * Called by:
     * - sendSnapshot()
     */
    private List<File> processFiles(String path) {
        List<File> fileList = new ArrayList<>();
        File directory = new File(path);

        
        File[] files = directory.listFiles();
        // If the directory is empty, the method returns an empty list.
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


    /*
     * This enum is responsible for the type of connection.
     * 
     * Called by:
     * - sendFolderList()
     * - sendFiles()
     */
    private enum ConnectionType {
        POST,
        FOLDER_LIST,
        FILE_MAP,
    }
}
