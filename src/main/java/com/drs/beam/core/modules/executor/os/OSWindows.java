/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.executor.os;

import com.drs.beam.core.modules.executor.OS;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.modules.ConfigModule;
import com.drs.beam.core.exceptions.ModuleInitializationException;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.util.config.ConfigParam;

/**
 *
 * @author Diarsid
 */
public class OSWindows implements OS {

    // Fields =============================================================================

    private final String PROGRAMS_LOCATION;
    private final IoInnerModule ioEngine;
    private final ExecutorService executorService;

    // Constructors =======================================================================
    public OSWindows(IoInnerModule io, ConfigModule config) {
        this.ioEngine = io;
        if(!Desktop.isDesktopSupported()){
            this.ioEngine.reportErrorAndExitLater(
                    "java.awt.Desktop is not supported.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
        this.PROGRAMS_LOCATION = config.getParameter(ConfigParam.PROGRAMS_LOCATION)
                .replace("\\", "/")
                .toLowerCase()
                .intern();      
        this.executorService = Executors.newFixedThreadPool(3);
    }

    // Methods ============================================================================
    @Override
    public void openLocation(Location location) {
        if (this.checkIfDirectoryExists(location.getPath())){
            this.executorService.execute(
                    new RunnableDesktopOpenTask(
                            this.ioEngine,
                            location.getPath()));            
            this.ioEngine.reportMessage("opening...");
        }     
    }

    @Override
    public void openFileInLocation(String target, Location location) {
        // targetName pattern: myPr / myFil
        // corrected target name: myProject / myFile.ext or "" if not exists
        target = this.checkNameInDirectory(target, location.getPath(), 
                "File not found.", "Location`s path is invalid.");

        if (target.length() > 0) {
            this.executorService.execute(
                    new RunnableDesktopOpenTask(
                            this.ioEngine,
                            location.getPath() + "\\" + target));
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
            this.executorService.execute(
                    new RunnableRuntimeCommandTask(this.ioEngine, commandBuilder.toString()));
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
            this.executorService.execute(
                    new RunnableDesktopOpenTask(
                            this.ioEngine,
                            PROGRAMS_LOCATION + "\\" + program));
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
    
    @Override
    public void openUrlWithDefaultBrowser(String urlAddress){
        this.executorService.execute(
                new RunnableBrowserTask(this.ioEngine, urlAddress));
        this.ioEngine.reportMessage("browse...");
    }
    
    @Override
    public void openUrlWithGivenBrowser(String urlAddress, String browserName){
        browserName = this.checkNameInDirectory(
                browserName+"-browser", PROGRAMS_LOCATION, 
                "Browser not found.", "Program`s location not found.");
        if (browserName.length() > 0){
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder
                    .append("cmd /c start ")
                    .append(PROGRAMS_LOCATION.replace("/", "\\"))
                    .append("\\")
                    .append(browserName)
                    .append(" ")
                    .append(urlAddress);
            this.executorService.execute(
                    new RunnableRuntimeCommandTask(this.ioEngine, commandBuilder.toString()));
            this.ioEngine.reportMessage("browse...");
        } else {
            this.ioEngine.reportMessage("Check browser in programs location or try with another browser.");
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
        boolean containsAllFragments;
        for (String file : directory.list()) {
            containsAllFragments = true;
            for (String nameFragment : Arrays.asList(nameToFind.split("-"))) {
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
}
