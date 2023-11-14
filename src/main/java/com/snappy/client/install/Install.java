package com.snappy.client.install;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import com.snappy.client.ErrorManager;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions;

/*
 * This class is responsible for the installation of the application.
 * It can check if the application is installed and install it if it's not.
 * It can also uninstall the application.
 * 
 * WARNING: be sure to run the program as root when this class is called.
 *          It needs super-user privileges to work.
 */
public class Install {

    // This method checks if the application is installed.
    public static boolean isInstalled() {
        File file = new File("/etc/snappy");

        return file.exists();
    }

    /*
     * This constructor is responsible for the installation of the application.
     * 
     * Called by:
     * - App.checkInstallation()
     */
    public Install() {
        System.out.println("Be sure to run this program as root for the first time, otherwise it won't work.\n");
        System.out.print("OK");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        scanner.close();
    }

    /*
     * This method is responsible for the installation of the application.
     * 
     * Called by:
     * - App.checkInstallation()
     */
    public void install() {
        System.out.println("Installing Snappy...\n");

        // The following methods are responsible for the creation of the directories and files needed by the application.
        System.out.println("Creating Snappy directory...");
        makeSnappyDirectory();
        makeSnappyBin();
        makeSnapshotDir();

        // The following method is responsible for the creation of the shell file.
        System.out.println("Creating Snappy bin file...");
        makeShFile();

        // The following method is responsible for the creation of the config file.
        System.out.println("Creating config file...");
        System.out.print("Enter the server IP: ");
        Scanner scanner = new Scanner(System.in);
        String ip = scanner.nextLine();
        System.out.print("Enter the server port: ");
        int port = Integer.parseInt(scanner.nextLine());
        makeConfigFile(ip, port);
        scanner.close();

        System.out.println("Done!");
        System.exit(0);
    }

    /*
     * The following method is responsible for the creation of the main directory.
     * 
     * Called by:
     * - install()
     */
    private void makeSnappyDirectory() {
        File snappyDir = new File("/etc/snappy");
        if (!snappyDir.exists()) {
            snappyDir.mkdir();
        }
    }

    /*
     * The following method is responsible for the creation of the bin directory and the jar file.
     * 
     * Called by:
     * - install()
     */
    private void makeSnappyBin() {
        File snappyBin = new File("/etc/snappy/bin");
        if (!snappyBin.exists()) {
            snappyBin.mkdir();

            ProcessBuilder processBuilder = new ProcessBuilder("cp",
                                                               System.getProperty("user.dir") + "/snappy-0.1.jar",
                                                               "/etc/snappy/bin/snappy.jar");
            try {
                Process process = processBuilder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                ErrorManager.exitWithError("Something went wrong while copying the jar file. Report this error to the developer.", e);
            }
        }
    }

    /*
     * The following method is responsible for the creation of the snapshots directory.
     * 
     * Called by:
     * - install()
     */
    private void makeSnapshotDir() {
        File snappySnapshots = new File("/etc/snappy/snapshots");
        if (!snappySnapshots.exists()) {
            snappySnapshots.mkdir();
        }
    }

    /*
     * The following method is responsible for the creation of the shell file.
     * 
     * Called by:
     * - install()
     */
    private void makeShFile() {
        ClassLoader classLoader = getClass().getClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream("template.sh");

        // Checks if the template file exists.
        if (inputStream != null) {
            // Reads the template file.
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            scanner.close();

            // Writes the template file to the shell file.
            String content = stringBuilder.toString();
            try {
                FileWriter fileWriter = new FileWriter("/usr/bin/snappy");
                fileWriter.write(content);
                fileWriter.close();

                ProcessBuilder processBuilder = new ProcessBuilder("chmod", "+x", "/usr/bin/snappy");
                Process process = processBuilder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                ErrorManager.exitWithError("Something went wrong while creating the shell file. Report this error to the developer.", e);
            }
        }
    }

    /*
     * The following method is responsible for the creation of the config file.
     * 
     * Called by:
     * - install()
     * 
     * 
     * (Yeah, I know it shouldn't stay here. I'll move it up when I'll have time)
     */
    private void makeConfigFile(String ip, int port) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();

        data.put("version", "1.0");
        data.put("server_ip", ip);
        data.put("server_port", port);
        data.put("default_subvolume", "/");

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml finalYaml = new Yaml(options);
        try (FileWriter writer = new FileWriter("/etc/snappy/config.yml")) {
            finalYaml.dump(data, writer);
        } catch (IOException e) {
            ErrorManager.exitWithError("Something went wrong while creating the config file. Report this error to the developer.", e);
        }
    }


    

    /*
     * The following method is responsible for the creation of the config file.
     * 
     * Called by:
     * - CommandLine.parse()
     */
    public void uninstall() {
        System.out.println("Uninstalling Snappy...\n");

        System.out.println("Removing Snappy directory...");
        removeSnappyDirectory();

        System.out.println("Removing Snappy bin file...");
        removeShFile();

        System.out.println("Done!");
        System.exit(0);
    }

    /*
     * The following method is responsible for the removal of the main directory.
     * 
     * Called by:
     * - uninstall()
     */
    private void removeSnappyDirectory() {
        File snappyDir = new File("/etc/snappy");
        if (snappyDir.exists()) {
            ProcessBuilder processBuilder = new ProcessBuilder("rm", "-rf", "/etc/snappy");
            try {
                Process process = processBuilder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                ErrorManager.exitWithError("Something went wrong while removing the Snappy directory. Report this error to the developer.", e);
            }
        }
    }

    /*
     * The following method is responsible for the removal of the shell file.
     * 
     * Called by:
     * - uninstall()
     */
    private void removeShFile() {
        File shFile = new File("/usr/bin/snappy");
        if (shFile.exists()) {
            ProcessBuilder processBuilder = new ProcessBuilder("rm", "-f", "/usr/bin/snappy");
            try {
                Process process = processBuilder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                ErrorManager.exitWithError("Something went wrong while removing the Snappy shell file. Report this error to the developer.", e);
            }
        }
    }
}
