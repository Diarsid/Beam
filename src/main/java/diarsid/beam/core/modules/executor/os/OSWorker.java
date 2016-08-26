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

import diarsid.beam.core.entities.local.Location;
import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.executor.IntelligentExecutorCommandContext;
import diarsid.beam.core.modules.executor.OS;
import diarsid.beam.core.modules.executor.os.actions.SystemActionsExecutor;
import diarsid.beam.core.modules.executor.os.search.FileSearcher;
import diarsid.beam.core.modules.executor.os.search.result.FileSearchResult;
import diarsid.beam.core.modules.executor.workflow.OperationResult;
import diarsid.beam.shared.modules.ConfigModule;
import diarsid.beam.shared.modules.config.Config;

import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.failByInvalidArgument;
import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.failByInvalidLogic;
import static diarsid.beam.core.modules.executor.workflow.OperationResultImpl.success;

/**
 *
 * @author Diarsid
 */
public class OSWorker implements OS {

    private final String programsLocationPath;
    private final IoInnerModule ioEngine;
    private final SystemActionsExecutor actionsExecutor;
    private final FileSearcher fileSearcher;
    private final IntelligentExecutorCommandContext intelligentContext;

    public OSWorker(
            IoInnerModule io, 
            ConfigModule config, 
            SystemActionsExecutor actionsExecutor,
            FileSearcher fileSearcher,
            IntelligentExecutorCommandContext intelligentContext) {
        
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
        this.intelligentContext = intelligentContext;
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
    public OperationResult openLocation(Location location) {
        if ( this.checkIfDirectoryExists(location.getPath()) ) {
            this.actionsExecutor.asyncOpenWithDesktop(location.getPath());
            this.ioEngine.reportMessage("opening...");
            return success();
        } else {
            this.ioEngine.reportMessage("This location does not exists.");
            return failByInvalidArgument(location.getName());
        }    
    }

    @Override
    public OperationResult openFileInLocation(String target, Location location) {
        // targetName pattern: myPr / myFil
        // corrected target name: myProject / myFile.ext or "" if not exists
        FileSearchResult result = this.fileSearcher.findTarget(target, location.getPath());
        String resolvedTarget;
        
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                resolvedTarget = result.success().getFoundFile();
            } else {
                resolvedTarget = this.chooseOneVariantFrom(
                        target, result.success().getMultipleFoundFiles());                
            } 
            if ( resolvedTarget.isEmpty() ) {
                return success();
            }
        } else {
            if ( result.failure().targetNotFound() ) {
                this.ioEngine.reportMessage(target + " not found in " + location.getName());
                return failByInvalidArgument(target);
            }
            if ( result.failure().locationNotFound() ) {
                this.ioEngine.reportMessage(
                        "Location " + location.getName() + "<" + location.getPath() + "> not found");
                return failByInvalidArgument(location.getName());
            }
            if ( result.failure().targetNotAccessible() ) {
                this.ioEngine.reportMessage("Access denied to " + target);
                return failByInvalidArgument(target);
            }
            return failByInvalidLogic();
        }        
        
        this.actionsExecutor.asyncOpenWithDesktop(
                location.getPath() + "/" + resolvedTarget);
        this.ioEngine.reportMessage("opening...");
        return success();
    }

    @Override
    public OperationResult openFileInLocationWithProgram(
            String file, Location location, String program) {
        
        FileSearchResult fileResult = this.fileSearcher.findTarget(file, location.getPath());
        FileSearchResult programResult = this.fileSearcher.findTarget(program, programsLocationPath);
        
        String resolvedFile;
        String resolvedProgram;
        
        if ( fileResult.isOk() ) {
            if ( fileResult.success().hasSingleFoundFile() ) {
                resolvedFile = fileResult.success().getFoundFile();
            } else {
                resolvedFile = this.chooseOneVariantFrom(
                        file, fileResult.success().getMultipleFoundFiles());
            }
            if ( resolvedFile.isEmpty() ) {
                return success();
            }
        } else {
            if ( fileResult.failure().targetNotFound() ) {
                this.ioEngine.reportMessage(file + " not found in " + location.getName());
                return failByInvalidArgument(file);
            }
            if ( fileResult.failure().locationNotFound() ) {
                this.ioEngine.reportMessage(
                        "Location " + location.getName() + "<" + location.getPath() + "> not found");
                return failByInvalidArgument(location.getPath());
            }
            if ( fileResult.failure().targetNotAccessible() ) {
                this.ioEngine.reportMessage("Access denied to " + file);
                return failByInvalidArgument(file);
            }
            return failByInvalidLogic();
        }
        
        if ( programResult.isOk() ) {
            if ( programResult.success().hasSingleFoundFile() ) {
                resolvedProgram = programResult.success().getFoundFile();
            } else {
                resolvedProgram = this.chooseOneVariantFrom(
                        program, programResult.success().getMultipleFoundFiles());
            }
            if ( resolvedProgram.isEmpty() ) {
                return success();
            }
        } else {
            if ( programResult.failure().targetNotFound() ) {
                this.ioEngine.reportMessage("Program " + program + " not found");
                return failByInvalidArgument(program);
            }
            if ( programResult.failure().locationNotFound() ) {
                this.ioEngine.reportMessage(
                        "Programs location " + programsLocationPath + " does not exist");
                return failByInvalidArgument(programsLocationPath);
            }
            if ( programResult.failure().targetNotAccessible() ) {
                this.ioEngine.reportMessage("Program " + program + " is not accessible");
                return failByInvalidArgument(program);
            }
            return failByInvalidLogic();
        }
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder
                .append("cmd /c start ")
                .append(programsLocationPath.replace("/", "\\"))
                .append("\\")
                .append(resolvedProgram)
                .append(" ")
                .append(location.getPath().replace("/", "\\"))
                .append("\\")
                .append(resolvedFile);
        this.actionsExecutor.asyncExecuteWithRuntime(commandBuilder.toString());
        this.ioEngine.reportMessage("opening with...");
        return success();
    }

    @Override
    public OperationResult runProgram(String program) {
        // program pattern: notep, NetBe
        // corrected program names: notepad.exe, NetBeans.lnk or "" if not exists
        FileSearchResult result = this.fileSearcher.findTarget(program, programsLocationPath);
        String resolvedProgram;
        
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                resolvedProgram = result.success().getFoundFile();
            } else {
                resolvedProgram = this.chooseOneVariantFrom(
                        program, result.success().getMultipleFoundFiles());
            }
            if ( resolvedProgram.isEmpty() ) {
                return success();
            }
        } else {
            if ( result.failure().targetNotFound() ) {
                this.ioEngine.reportMessage("Program " + program + " not found");
                return failByInvalidArgument(program);
            }
            if ( result.failure().locationNotFound() ) {
                this.ioEngine.reportMessage(
                        "Programs location " + programsLocationPath + " does not exist");
                return failByInvalidArgument(programsLocationPath);
            }
            if ( result.failure().targetNotAccessible() ) {
                this.ioEngine.reportMessage("Program " + program + " is not accessible");
                return failByInvalidArgument(program);
            }
            return failByInvalidLogic();
        }
        
        this.actionsExecutor.asyncOpenWithDesktop(programsLocationPath + "/" + resolvedProgram);
        this.ioEngine.reportMessage("running...");
        return success();
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
        this.actionsExecutor.asyncBrowseWithDesktop(urlAddress);
        this.ioEngine.reportMessage("browsing...");
        return success();
    }
    
    @Override
    public OperationResult openUrlWithGivenBrowser(
            String urlAddress, String browser) {
        
        FileSearchResult result = this.fileSearcher.findTarget(
                browser+"-browser", programsLocationPath);
        String resolvedBrowser;
        
        if ( result.isOk() ) {
            if ( result.success().hasSingleFoundFile() ) {
                resolvedBrowser = result.success().getFoundFile();
            } else {
                resolvedBrowser = this.chooseOneVariantFrom(
                        browser, result.success().getMultipleFoundFiles());
            }
            if ( resolvedBrowser.isEmpty() ) {
                return success();
            }
        } else {
            if ( result.failure().targetNotFound() ) {
                this.ioEngine.reportMessage("Browser " + browser + " not found");
                return failByInvalidArgument(browser);
            }
            if ( result.failure().locationNotFound() ) {
                this.ioEngine.reportMessage(
                        "Programs location " + programsLocationPath + " does not exist");
                return failByInvalidArgument(programsLocationPath);
            }
            if ( result.failure().targetNotAccessible() ) {
                this.ioEngine.reportMessage("Browser " + browser + " is not accessible");
                return failByInvalidArgument(browser);
            }
            return failByInvalidLogic();
        }
        
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder
                .append("cmd /c start ")
                .append(programsLocationPath.replace("/", "\\"))
                .append("\\")
                .append(resolvedBrowser)
                .append(" ")
                .append(urlAddress);
        this.actionsExecutor.asyncExecuteWithRuntime(commandBuilder.toString());
        this.ioEngine.reportMessage("browse...");
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
