/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.os.listing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

import static java.nio.file.Files.newDirectoryStream;

import static diarsid.beam.core.os.listing.FolderType.LIST_OF_EXECUTABLES;
import static diarsid.beam.core.os.listing.FolderType.PROGRAM_FOLDER;
import static diarsid.beam.core.os.listing.FolderType.PROJECT_FOLDER;
import static diarsid.beam.core.os.listing.FolderType.RESTRICTED_FOLDER;
import static diarsid.beam.core.os.listing.FolderType.USUAL_FOLDER;

/**
 *
 * @author Diarsid
 */
class FolderTypeDetector {
    
    private final FileItemAnalizer analizer;
    
    FolderTypeDetector(FileItemAnalizer analizer) {
        this.analizer = analizer;        
    }
    
    FolderType examineTypeOf(Path folder) throws IOException {
        if ( this.analizer.isRestrictedFolder(folder) ) {
            return RESTRICTED_FOLDER;
        }
        int programSpecificFilesCount = 0;
        int programSpecificFoldersCount = 0;
        Iterator iterator = newDirectoryStream(folder).iterator();
        Path iterated;
        for (int i = 0; i < 10 && iterator.hasNext(); i++) {
            iterated = (Path) iterator.next();
            //debug("[PROGRAM FOLDER DETECTOR] examine : " + iterated.toString());
            if ( this.analizer.isProgramSpecificFile(iterated) ) {
                //debug("[PROGRAM FOLDER DETECTOR]    - specific file!");
                programSpecificFilesCount++;
            }
            if ( this.analizer.isProjectSpecificFile(iterated) ) {
                return PROJECT_FOLDER;
            }
            if ( this.analizer.isProgramSpecificFolder(iterated) ) {
                //debug("[PROGRAM FOLDER DETECTOR]    - specific folder!");
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
}
