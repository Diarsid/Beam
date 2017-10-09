/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

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
    private final Detector detector;
    private final List<String> collected;
    
    FileVisitorForCollecting(
            FileSearchMode fileSearchMode, 
            Path root,
            Detector detector) {
        this.fileSearchMode = fileSearchMode;
        this.root = root;
        this.detector = detector;
        this.collected = new ArrayList<>();
    }
    
    List<String> collected() {
        return this.collected;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        dir = this.root.relativize(dir);
        
        switch ( this.fileSearchMode ) {
            case FILES_ONLY : {
                return CONTINUE;
            }
            case ALL :
            case FOLDERS_ONLY :
            default : {
                if ( this.detector.isMatch(dir) ) {
                    this.collected.add(normalizeSeparators(dir.toString()));
                }
                return CONTINUE;
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
                if ( this.detector.isMatch(file) ) {
                    this.collected.add(normalizeSeparators(file.toString()));
                }
                return CONTINUE;
            }
        }
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path dir, IOException e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
        return CONTINUE;
    }
    
    private boolean isSystemFile(Path path) {
        return containsIgnoreCase(asName(path), "desktop.ini");
    }
}
