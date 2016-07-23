/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor.os;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import diarsid.beam.core.util.Logs;
import diarsid.beam.core.entities.local.Location;
import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.workflow.OperationResult;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

import static diarsid.beam.core.modules.executor.workflow.OperationResult.failByInvalidArgument;
import static diarsid.beam.core.modules.executor.workflow.OperationResult.failByInvalidLogic;
import static diarsid.beam.core.modules.executor.workflow.OperationResult.success;

/**
 *
 * @author Diarsid
 */
public class OSWindows implements OS {

    private final String PROGRAMS_LOCATION;
    private final IoInnerModule ioEngine;
    private final ExecutorService executorService;
    private final SearchFileVisitor searchVisitor;
    private final IntelligentExecutorCommandContext intelligentContext;

    public OSWindows(
            IoInnerModule io, 
            ConfigModule config, 
            IntelligentExecutorCommandContext intelligentContext) {
        
        this.ioEngine = io;
        if(!Desktop.isDesktopSupported()){
            this.ioEngine.reportErrorAndExitLater(
                    "java.awt.Desktop is not supported.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
        this.PROGRAMS_LOCATION = config.get(Config.PROGRAMS_LOCATION)
                .replace("\\", "/")
                .toLowerCase()
                .intern();      
        this.executorService = Executors.newFixedThreadPool(3);
        this.searchVisitor = new SearchFileVisitor(this.ioEngine);
        this.intelligentContext = intelligentContext;
    }

    @Override
    public OperationResult openLocation(Location location) {
        if (this.checkIfDirectoryExists(location.getPath())){
            this.executorService.execute(
                    new RunnableDesktopOpenTask(
                            this.ioEngine,
                            location.getPath()));            
            this.ioEngine.reportMessage("opening...");
            return success();
        } else {
            this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                    "open", location.getName());
            return failByInvalidArgument(location.getName());
        }    
    }

    @Override
    public OperationResult openFileInLocation(String target, Location location) {
        // targetName pattern: myPr / myFil
        // corrected target name: myProject / myFile.ext or "" if not exists
        String checkedTarget = this.checkNameInDirectory(
                target, 
                location.getPath(), 
                "File not found.", 
                "Location`s path is invalid.");
        
        if (checkedTarget.length() > 0) {
            this.executorService.execute(
                    new RunnableDesktopOpenTask(
                            this.ioEngine,
                            location.getPath() + "/" + checkedTarget));            
            this.ioEngine.reportMessage("opening...");
            return success();
        } else {
            return failByInvalidArgument(target);
        }
    }

    @Override
    public OperationResult openFileInLocationWithProgram(
            String givenFile, Location givenLocation, String givenProgram) {
        
        String checkedFile = this.checkNameInDirectory(
                givenFile, 
                givenLocation.getPath(), 
                "File not found.", 
                "Location`s path is invalid.");
        String checkedProgram = this.checkNameInDirectory(
                givenProgram, 
                PROGRAMS_LOCATION,
                "Program not found.", 
                "Program`s location not found.");

        if ( checkedFile.length() > 0 && checkedProgram.length() > 0 ) {
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder
                    .append("cmd /c start ")
                    .append(PROGRAMS_LOCATION.replace("/", "\\"))
                    .append("\\")
                    .append(checkedProgram)
                    .append(" ")
                    .append(givenLocation.getPath().replace("/", "\\"))
                    .append("\\")
                    .append(checkedFile);
            this.executorService.execute(
                    new RunnableRuntimeCommandTask(
                            this.ioEngine, commandBuilder.toString()));
            this.ioEngine.reportMessage("running...");
            return success();
        } else {
            if ( givenFile.isEmpty() ) {
                return failByInvalidArgument(givenFile);
            } else if ( givenProgram.isEmpty() ) {
                return failByInvalidArgument(givenProgram);
            } else {
                return failByInvalidLogic();
            }            
        }
    }

    @Override
    public OperationResult runProgram(String givenProgram) {
        // program pattern: notep, NetBe
        // corrected program names: notepad.exe, NetBeans.lnk or "" if not exists
        String checkedProgram = this.checkNameInDirectory(
                givenProgram, 
                PROGRAMS_LOCATION,
                "Program not found.", 
                "Program`s location not found.");

        if ( checkedProgram.length() > 0 ) {
            this.executorService.execute(
                    new RunnableDesktopOpenTask(
                            this.ioEngine,
                            PROGRAMS_LOCATION + "\\" + checkedProgram));
            this.ioEngine.reportMessage("running...");
            return success();
        } else {
            return failByInvalidArgument(givenProgram);
        }
    }

    private boolean checkIfDirectoryExists(String location) {
        File dir = new File(location);
        if ( ! dir.exists() ) {
            this.ioEngine.reportMessage("This path doesn`t exist.");
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
        if ( ! dir.exists() ) {
            this.ioEngine.reportError("This path does not exist.");
            return null;
        }
        if ( ! dir.isDirectory() ) {
            this.ioEngine.reportError("This location is not a directory.");
            return null;
        }
        
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
    }
    
    @Override
    public OperationResult openUrlWithDefaultBrowser(String urlAddress) {
        this.executorService.execute(
                new RunnableBrowserTask(this.ioEngine, urlAddress));
        this.ioEngine.reportMessage("browse...");
        return success();
    }
    
    @Override
    public OperationResult openUrlWithGivenBrowser(
            String urlAddress, String givenBrowserName) {
        
        String checkedBrowserName = this.checkNameInDirectory(
                givenBrowserName+"-browser", 
                PROGRAMS_LOCATION, 
                "Browser not found.", 
                "Program`s location not found.");
        if ( checkedBrowserName.length() > 0 ) {
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder
                    .append("cmd /c start ")
                    .append(PROGRAMS_LOCATION.replace("/", "\\"))
                    .append("\\")
                    .append(checkedBrowserName)
                    .append(" ")
                    .append(urlAddress);
            this.executorService.execute(
                    new RunnableRuntimeCommandTask(
                            this.ioEngine, commandBuilder.toString()));
            this.ioEngine.reportMessage("browse...");
            return success();
        } else {
            this.ioEngine.reportMessage("Check browser in programs location"
                    + " or try with another browser.");
            this.intelligentContext.discardCurrentlyExecutedCommandInPatternAndOperation(
                    "see", givenBrowserName);
            return failByInvalidArgument(givenBrowserName);
        }
    }

    private String checkNameInDirectory(
            String targetName,
            String location,
            String noTargetMessage,
            String noLocationMessage) {

        Path dir = Paths.get(location);
        String result = "";
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            if (this.containsFileSeparator(targetName)) {
                result = this.searchByGivenPath(dir, targetName);
            } else {
                result = this.searchByFileName(dir, targetName);
            }
            if ( result.isEmpty() ) {
                this.ioEngine.reportMessage(
                        noTargetMessage + " in location::" + location);   
                this.intelligentContext
                        .discardCurrentlyExecutedCommandInPattern(targetName);
            }
        } else {
            this.ioEngine.reportMessage(noLocationMessage);
            this.intelligentContext
                    .discardCurrentlyExecutedCommandInPattern(location);
        }        
        return result;
    }
    
    private boolean containsFileSeparator(String target) {
        return target.contains("/") || target.contains("\\");
    }
    
    private String searchByGivenPath(Path dir, String targetName) {
        Logs.debug("[OS] search by path: " + dir.resolve(targetName).toString());
        if ( Files.exists(dir.resolve(targetName)) ) {
            return targetName;
        } else {
            return "";
        }
    }

    private String searchByFileName(Path dir, String targetName) {        
        List<String> foundItems = new ArrayList<>();        
        this.findInTree(dir, targetName, foundItems);
        
        if (foundItems.size() == 1) {
            return foundItems.get(0);
        } else if (foundItems.size() > 1) {
            return this.chooseOneVariantFrom(targetName, foundItems);
        } else {
            return "";
        }
    }
    
    private void findInTree(Path root, String nameToFind, List<String> foundItems) {
        try { 
            
            Files.walkFileTree(
                    root, 
                    EnumSet.of(FileVisitOption.FOLLOW_LINKS), 
                    2,
                    this.searchVisitor.set(root, nameToFind, foundItems));
            
            this.searchVisitor.clear();
            foundItems.remove("");
        } catch (AccessDeniedException e ) {
            this.ioEngine.reportError("Access to file is denied, stream stoped.");
            this.intelligentContext
                    .discardCurrentlyExecutedCommandInPattern(nameToFind);
        } catch (IOException e ) {
            this.ioEngine.reportError("Error occured in Files.find(...);");
            this.intelligentContext
                    .discardCurrentlyExecutedCommandInPattern(nameToFind);
        }
    }

    private String chooseOneVariantFrom(
            String targetName, List<String> variants) {
        
        int variantNumber = this.intelligentContext.resolve(
                "There are several targets:",
                targetName,
                variants);
        if (variantNumber > 0) {
            return variants.get(variantNumber - 1);
        } else if (variantNumber == -1) {
            return "";
        } else if (variantNumber == -2) {
            this.ioEngine.reportError("There is no external IO engine now.");
            return "";
        } else {
            return "";
        }
    }
    
    @Override
    public void createAndOpenTxtFileIn(String name, Location location) {
        try {
            Path newNote;
            if (name.isEmpty()) {
                name = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("HH-mm-ss-(dd-MM-uuuu)"));
            } 
            newNote = Paths.get(location.getPath() + "/" + name + ".txt");
            if (name.contains("/") || name.contains("\\")) {
                if ( ! Files.exists(newNote.getParent()) ) {
                    Files.createDirectories(newNote.getParent());
                }
            }
            if (Files.exists(newNote)) {
                ioEngine.reportMessage("This note already exists.");
            } else {
                Files.createFile(newNote);
                this.executorService.submit(
                        new RunnableDesktopOpenTask(
                                ioEngine, 
                                newNote.toFile().getPath()));
            }
        } catch (IOException e) {
            this.ioEngine.reportError("Unknown IOException occured.");
        }
    }
}
