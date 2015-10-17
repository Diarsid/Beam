/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.executor.os;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.drs.beam.server.entities.location.Location;
import com.drs.beam.server.modules.ModuleInitializationException;
import com.drs.beam.server.modules.io.InnerIOModule;
import com.drs.beam.util.config.ConfigContainer;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
public class OSWindows implements OS {

    // Fields =============================================================================

    private final String PROGRAMS_LOCATION;
    private final InnerIOModule ioEngine;
    private final Desktop desktop;
    private final Runtime runtime;

    // Constructors =======================================================================
    public OSWindows(InnerIOModule io) {
        this.ioEngine = io;
        if(!Desktop.isDesktopSupported()){
            this.ioEngine.reportErrorAndExitLater(
                    "java.awt.Desktop is not supported.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
        this.PROGRAMS_LOCATION = ConfigContainer.getParam(ConfigParam.PROGRAMS_LOCATION)
                .replace("\\", "/")
                .toLowerCase()
                .intern();        
        this.desktop = Desktop.getDesktop();
        this.runtime = Runtime.getRuntime();
    }

    // Methods ============================================================================
    @Override
    public void openLocation(Location location) {
        if (this.checkIfDirectoryExists(location.getPath())){
            this.runDesktopTask(location.getPath());
            this.ioEngine.reportMessage("opening...");
        }     
    }

    @Override
    public void openFileInLocation(String file, Location location) {
        // targetName pattern: myPr / myFil
        // corrected target name: myProject / myFile.ext or "" if not exists
        file = this.checkNameInDirectory(file, location.getPath(), "File not found.", "Location`s path is invalid.");

        if (file.length() > 0) {
            String path = location.getPath() + "\\" + file;
            this.runDesktopTask(path);
            this.ioEngine.reportMessage("opening...");
        }
    }

    @Override
    public void openFileInLocationWithProgram(String file, Location location, String program) {
        file = this.checkNameInDirectory(file, location.getPath(), "File not found.", "Location`s path is invalid.");
        program = this.checkNameInDirectory(program, PROGRAMS_LOCATION,
                "Program not found.", "Program`s location not found.");

        if (file.length() > 0 && program.length() > 0) {
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder
                    .append("cmd /c start ")
                    .append(PROGRAMS_LOCATION.replace("/", "\\"))
                    .append("\\")
                    .append(program)
                    .append(" ")
                    .append(location.getPath().replace("/", "\\"))
                    .append("\\")
                    .append(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runtime.exec(commandBuilder.toString());
                    } catch (IOException e) {
                        ioEngine.reportException(e, "IOException: open file with program.");
                    } catch (IllegalArgumentException argumentException) {
                        ioEngine.reportError("Unknown target");
                    }
                }
            }).start();
            this.ioEngine.reportMessage("running...");
        }
    }

    @Override
    public void runProgram(String program) {
        // program pattern: notep, NetBe
        // corrected program names: notepad.exe, NetBeans.lnk or "" if not exists
        program = this.checkNameInDirectory(program, PROGRAMS_LOCATION,
                "Program not found.", "Program`s location not found.");

        if (program.length() > 0) {
            String path = PROGRAMS_LOCATION + "\\" + program;
            this.runDesktopTask(path);
            this.ioEngine.reportMessage("running...");
        }
    }

    @Override
    public boolean checkIfDirectoryExists(String location) {
        File dir = new File(location);
        if (!dir.exists()) {
            this.ioEngine.reportMessage("This path doesn`t exists.");
            return false;
        } else if (!dir.isDirectory()) {
            this.ioEngine.reportMessage("This isn`t a directory.");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<String> getLocationContent(Location location) {
        File dir = new File(location.getPath());
        if (dir.exists() && dir.isDirectory()) {
            File[] list = dir.listFiles();
            List<String> content = new ArrayList<>();
            int folderIndex = 0;
            for (File file : list) {
                if (file.isDirectory()) {
                    content.add(folderIndex, " [_] " + file.getName());
                    folderIndex++;
                } else {
                    content.add("  o  " + file.getName());
                }
            }
            content.remove("  o  desktop.ini");
            return content;
        } else {
            this.ioEngine.reportError("This location does not exists or is not a directory.");
            return null;
        }
    }

    private String checkNameInDirectory(
            String targetName,
            String location,
            String noTargetMessage,
            String noLocationMessage) {

        File dir = new File(location);
        if (dir.exists() && dir.isDirectory()) {
            List<String> foundItems = new ArrayList<>();

            if (targetName.contains("-")) {
                this.findItemsInDirectoryByNameParts(dir, targetName, foundItems);
            } else {
                this.findItemsInDirectoryByName(dir, targetName, foundItems);
            }

            if (foundItems.size() == 1) {
                return foundItems.get(0);
            } else if (foundItems.size() > 1) {
                return this.chooseOneVariantFrom(foundItems);
            } else {
                this.ioEngine.reportMessage(noTargetMessage);
                return "";
            }
        } else {
            this.ioEngine.reportMessage(noLocationMessage);
            return "";
        }
    }

    private List<String> findItemsInDirectoryByName(File directory, String nameToFind, List<String> foundItems) {
        for (String element : directory.list()) {
            if (element.toLowerCase().contains(nameToFind)) {
                foundItems.add(element);
            }
        }
        return foundItems;
    }

    private List<String> findItemsInDirectoryByNameParts(File directory, String nameToFind, List<String> foundItems) {
        List<String> fileNameFragments = Arrays.asList(nameToFind.split("-"));
        boolean containsAllFragments;
        for (String file : directory.list()) {
            containsAllFragments = true;
            for (String nameFragment : fileNameFragments) {
                if (!file.toLowerCase().contains(nameFragment)) {
                    containsAllFragments = false;
                    break;
                }
            }
            if (containsAllFragments) {
                foundItems.add(file);
            }
        }
        return foundItems;
    }

    private String chooseOneVariantFrom(List<String> variants) {
        int variantNumber = this.ioEngine.resolveVariantsWithExternalIO(
                "There are several targets:",
                variants);
        if (variantNumber > 0) {
            return variants.get(variantNumber - 1);
        } else if (variantNumber == -1) {
            return "";
        } else if (variantNumber == -2) {
            this.ioEngine.reportError("There are no external IO engine now.");
            return "";
        } else {
            return "";
        }
    }

    private void runDesktopTask(String target) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    desktop.open(new File(target));
                } catch (IOException e) {
                    ioEngine.reportException(e, "IOException: open target path with Desktop.");
                } catch (IllegalArgumentException argumentException) {
                    ioEngine.reportError("Unknown target");
                }
            }
        }).start();
    }
}
