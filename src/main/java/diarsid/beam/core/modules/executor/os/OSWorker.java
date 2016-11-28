/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.executor.os;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.entities.local.Location;
import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.context.ExecutorContext;
import diarsid.beam.core.modules.executor.os.actions.SystemActionsExecutor;
import diarsid.beam.core.modules.executor.os.listing.FileLister;
import diarsid.beam.core.modules.executor.os.search.FileSearcher;
import diarsid.beam.core.modules.executor.os.search.result.FileSearchFailure;
import diarsid.beam.core.modules.executor.os.search.result.FileSearchResult;
import diarsid.beam.core.modules.executor.os.search.result.FileSearchSuccess;
import diarsid.beam.core.modules.executor.workflow.OperationResult;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

import static java.util.Optional.of;

import static diarsid.beam.core.modules.executor.os.search.FileSearchMode.ALL;
import static diarsid.beam.core.modules.executor.os.search.FileSearchMode.FILES_ONLY;
import static diarsid.beam.core.modules.executor.os.search.FileSearchMode.FOLDERS_ONLY;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.combinePathFrom;
import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.success;
import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
public class OSWorker implements OS {

    private final String programsLocationPath;
    private final IoInnerModule ioEngine;
    private final SystemActionsExecutor actionsExecutor;
    private final FileSearcher fileSearcher;
    private final FileLister fileLister;
    private final ExecutorContext context;

    public OSWorker(
            IoInnerModule io, 
            ConfigModule config, 
            SystemActionsExecutor actionsExecutor,
            FileSearcher fileSearcher,
            FileLister fileLister,
            ExecutorContext intelligentContext) {
        
        this.ioEngine = io;
        if(!Desktop.isDesktopSupported()){
            this.ioEngine.reportErrorAndExitLater(
                    "java.awt.Desktop is not supported.",
                    "Program will be closed.");
            throw new ModuleInitializationException();
        }
        this.programsLocationPath = config.get(Config.PROGRAMS_LOCATION)
                .replace("\\", "/")
                .toLowerCase()
                .intern();      
        this.actionsExecutor = actionsExecutor;
        this.fileSearcher = fileSearcher;
        this.fileLister = fileLister;
        this.context = intelligentContext;
    }    
    
    private String chooseOneVariantFrom(
            String targetName, List<String> variants) {
                
        int variantNumber = this.context.resolve(
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
    public void openLocation(Location location) {
        if ( this.checkIfDirectoryExists(location.getPath()) ) {
            this.actionsExecutor.asyncOpenWithDesktop(location.getPath());
            this.ioEngine.reportMessage("opening...");
        } else {
            this.ioEngine.reportMessage("This location does not exists.");
            this.context.discardCurrentlyExecutedCommandByInvalidLocation(location.getName());
        }    
    }

    @Override
    public void openFileInLocation(String target, Location location) {
        // targetName pattern: myPr / myFil
        // corrected target name: myProject / myFile.ext or "" if not exists
        FileSearchResult result = this.fileSearcher
                .findTarget(target, location.getPath(), ALL);
        if ( result.isOk() ) {
            this.processSuccess(result.success(), target, location);
        } else {
            this.processFailure(result.failure(), target, location);
        }   
    }

    private void processSuccess(FileSearchSuccess success, String target, Location location) {
        String resolvedTarget;
        if ( success.hasSingleFoundFile() ) {
            resolvedTarget = success.getFoundFile();
        } else {
            resolvedTarget = this.chooseOneVariantFrom(
                    target, success.getMultipleFoundFiles());
            debug("[OS] target resolved: " + target + " -> " + resolvedTarget);
        }
        if ( ! resolvedTarget.isEmpty() ) {
            this.context.adjustCurrentlyExecutedCommand("open", target, "in", location.getName());
            this.actionsExecutor.asyncOpenWithDesktop(
                    location.getPath() + "/" + resolvedTarget);
            this.ioEngine.reportMessage("opening...");
        }        
    }

    private void processFailure(FileSearchFailure failure, String target, Location location) {
        if ( failure.targetNotFound() ) {
            this.ioEngine.reportMessage(target + " not found in " + location.getName());
            this.context.discardCurrentlyExecutedCommandByInvalidTarget(target);
        }
        if ( failure.locationNotFound() ) {
            this.ioEngine.reportMessage(
                    "Location " + location.getName() + "<" + location.getPath() + "> not found");
            this.context.discardCurrentlyExecutedCommandByInvalidLocation(location.getName());
        }
        if ( failure.targetNotAccessible() ) {
            this.ioEngine.reportMessage("Access denied to " + target);
            this.context.discardCurrentlyExecutedCommandByInvalidTarget(target);
        }
    }

    @Override
    public void runProgram(String program) {
        // program pattern: notep, NetBe
        // corrected program names: notepad.exe, NetBeans.lnk or "" if not exists
        FileSearchResult result = this.fileSearcher
                .findTarget(program, this.programsLocationPath, FILES_ONLY);        
        if ( result.isOk() ) {
            this.context.adjustCurrentlyExecutedCommand("run " + program);
            this.processProgramFound(result.success(), "run", program);
        } else {
            this.processProgramSearchingFailure(result.failure(), "run", program);
        }
    }    
    
    @Override
    public void runMarkedProgram(String program, String mark) {
        FileSearchResult result = this.fileSearcher
                .findTarget(program + "-" + mark, this.programsLocationPath, FILES_ONLY); 
        if ( result.isOk() ) {
            this.context.adjustCurrentlyExecutedCommand(mark + " " + program);
            this.processProgramFound(result.success(), mark, program);
        } else {
            this.processProgramSearchingFailure(result.failure(), mark, program);
        }
    }

    private void processProgramSearchingFailure(
            FileSearchFailure failure, String launchCommand, String program) {
        if ( failure.targetNotFound() ) {
            this.ioEngine.reportMessage("Program " + program + " not found");
            this.context.discardCurrentlyExecutedCommandInPatternAndOperation(
                    launchCommand, program);
        } else if ( failure.locationNotFound() ) {
            this.ioEngine.reportMessage(
                    "Programs location " + programsLocationPath + " does not exist");
        } else if ( failure.targetNotAccessible() ) {
            this.ioEngine.reportMessage("Program " + program + " is not accessible");
            this.context.discardCurrentlyExecutedCommandInPatternAndOperation(
                    launchCommand, program);
        }
    }

    private void processProgramFound(
            FileSearchSuccess success, String launchCommand, String program) {
        String resolvedProgram;
        if ( success.hasSingleFoundFile() ) {
            resolvedProgram = success.getFoundFile();
        } else {
            resolvedProgram = this.chooseOneVariantFrom(
                    program, success.getMultipleFoundFiles());
        }
        if ( ! resolvedProgram.isEmpty() ) {            
            this.actionsExecutor.asyncOpenWithDesktop(
                    this.programsLocationPath + "/" + resolvedProgram);
            this.ioEngine.reportMessage("running...");
        }        
    }
    
    private boolean checkIfDirectoryExists(String location) {
        File dir = new File(location);
        if ( ! dir.exists() ) {
            this.ioEngine.reportMessage("This path doesn`t exist.");
            return false;
        } else if ( ! dir.isDirectory()) {
            this.ioEngine.reportMessage("This isn`t a directory.");
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public List<String> listContentIn(Location location, int depth) {
        File dir = new File(location.getPath());
        if ( ! dir.exists() ) {
            this.ioEngine.reportError("This path does not exist.");
            return null;
        }
        if ( ! dir.isDirectory() ) {
            this.ioEngine.reportError("This location is not a directory.");
            return null;
        }        
        if ( depth == 0 ) {
            return null;
        }
        Optional<List<String>> listResult = this.fileLister
                .listContentOf(location, depth);        
        if ( listResult.isPresent() ) {
            return listResult.get();
        } else {
            this.ioEngine.reportMessage("...error while listing given path.");
            return null;
        }
    }
    
    @Override
    public List<String> listContentIn(Location location, String relativePath, int depth) {
        File dir = new File(location.getPath());
        if ( ! dir.exists() ) {
            this.ioEngine.reportError("This path does not exist.");
            return null;
        }
        if ( ! dir.isDirectory() ) {
            this.ioEngine.reportError("This location is not a directory.");
            return null;
        }        
        if ( depth == 0 ) {
            return null;
        }        
        
        Optional<List<String>> listResult;        
        FileSearchResult targetResult = 
                this.fileSearcher.findTarget(relativePath, location.getPath(), FOLDERS_ONLY);
        if ( targetResult.isOk() ) {
            listResult = this.listLocationAndPath(targetResult.success(), location, depth);
        } else {
            listResult = this.listLocation(targetResult.failure(), relativePath, location, depth);
        }
         
        if ( listResult.isPresent() ) {
            return listResult.get();
        } else {
            this.ioEngine.reportMessage("...error while listing given path.");
            return null;
        }
    }    

    private Optional<List<String>> listLocation(
            FileSearchFailure targetFailure, String relativePath, Location location, int depth) {
        Optional<List<String>> listResult;
        if ( targetFailure.targetNotFound() ) {
            this.ioEngine.reportMessage(relativePath + " not found in " + location.getName());
        } else if ( targetFailure.targetNotAccessible() ) {
            this.ioEngine.reportMessage(relativePath + " is not accessible.");
        }
        this.ioEngine.reportMessage("..." + location.getName() + " content:");
        listResult = this.fileLister.listContentOf(location, depth);
        return listResult;
    }

    private Optional<List<String>> listLocationAndPath(
            FileSearchSuccess targetFound, Location location, int depth) {
        String relativePath;
        Optional<List<String>> listResult;
        if ( targetFound.hasSingleFoundFile() ) {
            relativePath = targetFound.getFoundFile();
        } else {
            relativePath = this.resolveMultiplePaths(targetFound.getMultipleFoundFiles());
        }
        if ( ! relativePath.isEmpty() ) {
            listResult = this.fileLister.listContentOf(combinePathFrom(
            location.getPath(), relativePath), depth);
            return listResult;
        } else {
            return of(new ArrayList<>());
        }
    }
    
    private String resolveMultiplePaths(List<String> paths) {
        int choice = this.ioEngine.resolveVariants("...path to list?", paths);
        if ( choice > 0 ) {
            return paths.get(choice - 1);
        } else {
            return "";
        }
    }
    
    @Override
    public OperationResult openUrlWithDefaultBrowser(String urlAddress) {
        this.actionsExecutor.asyncBrowseWithDesktop(urlAddress);
        this.ioEngine.reportMessage("browsing...");
        return success();
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
                this.actionsExecutor.asyncOpenWithDesktop(newNote.toFile().getPath());
            }
        } catch (IOException e) {
            this.ioEngine.reportError("Unknown IOException occured.");
        }
    }
}
