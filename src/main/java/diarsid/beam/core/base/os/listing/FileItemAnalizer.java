/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.listing;

import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.asList;

import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsWordInIgnoreCase;

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
                asName(item), this.projectSpecificFiles);
    }
    
    boolean isRestrictedFolder(Path folder) {
        try {
            return containsWordInIgnoreCase(
                    this.restrictedSpecificFolder, asName(folder));
        } catch (NullPointerException e) {
            return containsIgnoreCaseAnyFragment(
                folder.toString(), this.programSpecificFileSigns);
        }
    }
    
    boolean isProgramSpecificFile(Path item) {
        return containsIgnoreCaseAnyFragment(
                asName(item), this.programSpecificFileSigns);
    }
    
    boolean isProgramSpecificFolder(Path item) {
        if ( isDirectory(item) ) {
            return containsWordInIgnoreCase(
                this.programSpecificFolders, asName(item));
        } else {
            return false;
        }        
    }
}
