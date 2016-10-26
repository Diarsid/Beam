/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.listing;

import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.asList;

import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsFullWordIgnoreCase;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;

/**
 *
 * @author Diarsid
 */
public class FileItemAnalizer {    
    
    private final List<String> programSpecificFileSigns;
    private final List<String> programSpecificFolders;
    private final List<String> restrictedSpecificFolder;
    private final List<String> projectSpecificFiles;
    
    public FileItemAnalizer() {
        this.programSpecificFileSigns = asList(new String[] {
            ".bat", ".sh", ".exe", ".bash", ".dll", 
            "conf", ".cfg", ".hbm", ".db", ".dat", 
            "license", "readme", ".log", ".git", 
            ".gitignore", ".gitattributes", "pom.xml", });
        this.projectSpecificFiles = asList(new String[] {
            ".project", ".pom", "pom.xml", "node_modules", 
            ".idea", "package.json", ".gitignore", ".gitattributes"});
        this.programSpecificFolders = asList(new String[] {
            "bin", "lib", "log", "conf", "config", "temp", "docs"});
        this.restrictedSpecificFolder = asList(new String[] {
            "nbproject", ".git", ".sonar", ".settings", 
            "src", "$RECYCLE.BIN"});
    }
    
    
    
    boolean isProjectSpecificFile(Path item) {
        return containsIgnoreCaseAnyFragment(
                item.getFileName().toString(), this.projectSpecificFiles);
    }
    
    boolean isRestrictedFolder(Path folder) {
        try {
            return containsFullWordIgnoreCase(
                    this.restrictedSpecificFolder, folder.getFileName().toString());
        } catch (NullPointerException e) {
            return containsIgnoreCaseAnyFragment(
                folder.toString(), this.programSpecificFileSigns);
        }
    }
    
    boolean isProgramSpecificFile(Path item) {
        return containsIgnoreCaseAnyFragment(
                item.getFileName().toString(), this.programSpecificFileSigns);
    }
    
    boolean isProgramSpecificFolder(Path item) {
        if ( isDirectory(item) ) {
            return containsFullWordIgnoreCase(
                this.programSpecificFolders, item.getFileName().toString());
        } else {
            return false;
        }        
    }
}