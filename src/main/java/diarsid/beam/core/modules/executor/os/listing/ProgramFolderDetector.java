/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.listing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.newDirectoryStream;
import static java.util.Arrays.asList;

import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;

/**
 *
 * @author Diarsid
 */
class ProgramFolderDetector {
    
    public static final int PROGRAM_FOLDER = 100;
    public static final int LIST_OF_EXECUTABLES = 101;
    public static final int USUAL_FOLDER = 50;
    
    private final List<String> programSpecificFileSigns;
    private final List<String> programSpecificFolders;
    
    ProgramFolderDetector() {
        this.programSpecificFileSigns = asList(new String[] {
            ".bat", ".sh", ".exe", ".bash", ".dll", 
            "conf", ".cfg", ".hbm", ".db", ".dat", 
            "license", "readme", ".log"});
        this.programSpecificFolders = asList(new String[] {
            "bin", "lib", "log", "conf", "temp", "docs"});
    }
    
    int examineTypeOf(Path folder) throws IOException {
        int programSpecificFilesCount = 0;
        int programSpecificFoldersCount = 0;
        for (Path item : newDirectoryStream(folder)) {
            if ( this.isProgramSpecificFile(item) ) {
                programSpecificFilesCount++;
            }
            if ( this.isProgramSpecificFolder(item) ) {
                programSpecificFoldersCount++;
            }
        }
        if ( programSpecificFoldersCount > 1 ) {
            return PROGRAM_FOLDER;
        } else if ( programSpecificFoldersCount == 0 ) {
            if ( programSpecificFilesCount > 0 ) {
                return LIST_OF_EXECUTABLES;
            } else {
                return USUAL_FOLDER;
            }
        } else if ( programSpecificFoldersCount == 1 ) {
            if ( programSpecificFilesCount > 0 ) {
                return PROGRAM_FOLDER;
            } else {
                return USUAL_FOLDER;
            }
        } else {
            return USUAL_FOLDER;
        }
    }
    
    private boolean isProgramSpecificFile(Path item) {
        return containsIgnoreCaseAnyFragment(
                item.getFileName().toString(), this.programSpecificFileSigns);
    }
    
    private boolean isProgramSpecificFolder(Path item) {
        return containsIgnoreCaseAnyFragment(
                item.getFileName().toString(), this.programSpecificFolders);
    }
}
