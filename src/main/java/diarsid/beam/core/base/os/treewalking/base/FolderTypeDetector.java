/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.treewalking.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;

import static java.nio.file.Files.newDirectoryStream;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.os.treewalking.base.FolderType.PROGRAM_FOLDER;
import static diarsid.beam.core.base.os.treewalking.base.FolderType.PROJECT_FOLDER;
import static diarsid.beam.core.base.os.treewalking.base.FolderType.RESTRICTED_FOLDER;
import static diarsid.beam.core.base.os.treewalking.base.FolderType.USUAL_FOLDER;


/**
 *
 * @author Diarsid
 */
public class FolderTypeDetector {
    
    private static final FolderTypeDetector DETECTOR;
    
    static {
        FileItemAnalizer analizer = new FileItemAnalizer(configuration());
        DETECTOR = new FolderTypeDetector(analizer);
    }
    
    private final FileItemAnalizer analizer;
    
    FolderTypeDetector(FileItemAnalizer analizer) {
        this.analizer = analizer;        
    }
    
    public static FolderTypeDetector getFolderTypeDetector() {
        return DETECTOR;
    }
    
    public FolderType safeExamineTypeOf(File file) {
        try {
            return examineTypeOf(file.toPath());
        } catch (IOException e) {
            return RESTRICTED_FOLDER;
        }
    }
    
    public FolderType examineTypeOf(Path folder) throws IOException {
        if ( this.analizer.isRestrictedFolder(folder) ) {
            return RESTRICTED_FOLDER;
        }
        try (DirectoryStream<Path> dirStream = newDirectoryStream(folder)) {
            return explorePathUsing(dirStream.iterator());
        }
    }
    
    private FolderType explorePathUsing(Iterator<Path> iterator) {
        int programSpecificElements = 0;
        int projectSpecificElements = 0;
        
        Path iterated;
        for (int i = 0; i < 10 && iterator.hasNext(); i++) {
            iterated = iterator.next();
            
            if ( this.analizer.isProjectDefinitiveElement(iterated) ) {
                return PROJECT_FOLDER;
            }
            
            if ( this.analizer.isProjectSpecificElement(iterated) ) {
                projectSpecificElements++;
                continue;
            }
            
            if ( this.analizer.isProgramSpecificElement(iterated) ) {
                programSpecificElements++;
                continue;
            }
        }
        
        if ( programSpecificElements == 0 && projectSpecificElements == 0 ) {
            return USUAL_FOLDER;
        }
        
        if ( projectSpecificElements == 0 && programSpecificElements > 0 ) {
            if ( programSpecificElements > 1 ) {
                return PROGRAM_FOLDER;
            }
        }
        
        if ( projectSpecificElements > 0 && programSpecificElements == 0 ) {
            if ( projectSpecificElements > 1 ) {
                return PROJECT_FOLDER;
            }
        }
        
        return USUAL_FOLDER;
    }
}
