/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import diarsid.beam.core.base.os.treewalking.base.FolderType;
import diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
class FileVisitorForCollecting extends SimpleFileVisitor<Path> {
    
    private final FileSearchMode fileSearchMode;
    private final Path root;
    private final NameDetector nameDetector;
    private final FolderTypeDetector folderTypeDetector;
    private final List<String> collected;
    
    FileVisitorForCollecting(
            FileSearchMode fileSearchMode, 
            Path root,
            NameDetector nameDetector, 
            FolderTypeDetector folderTypeDetector) {
        this.fileSearchMode = fileSearchMode;
        this.root = root;
        this.nameDetector = nameDetector;
        this.folderTypeDetector = folderTypeDetector;
        this.collected = new ArrayList<>();
    }
    
    List<String> collected() {
        return this.collected;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
            throws IOException {
        FolderType folderType = this.folderTypeDetector.examineTypeOf(dir);
        FileVisitResult continueMode;
        
        if ( folderType.isRestricted() ) {
            continueMode = SKIP_SUBTREE;
        } else {
            continueMode = CONTINUE;
        }
        
        dir = this.root.relativize(dir);
        
        switch ( this.fileSearchMode ) {
            case FILES_ONLY : {
                return continueMode;
            }
            case ALL :
            case FOLDERS_ONLY :
            default : {
                if ( this.nameDetector.isMatch(dir) ) {
                    this.collected.add(normalizeSeparators(dir.toString()));
                }
                return continueMode;
            }
        }
    }
    
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {        
        if ( this.isSystemFile(file) ) {
            return CONTINUE;
        }
        file = this.root.relativize(file);
        
        switch ( this.fileSearchMode ) {
            case FOLDERS_ONLY : {
                return CONTINUE;
            }
            case ALL :
            case FILES_ONLY :
            default : {
                if ( this.nameDetector.isMatch(file) ) {
                    this.collected.add(normalizeSeparators(file.toString()));
                }
                return CONTINUE;
            }
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path dir, IOException e) {
        // TODO MEDIUM
        System.out.println(e.getMessage());
        e.printStackTrace();
        return CONTINUE;
    }
    
    private boolean isSystemFile(Path path) {
        return containsIgnoreCase(asName(path), "desktop.ini");
    }
}
