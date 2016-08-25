/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.normalizePathFragmentsFrom;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.relativizeFileName;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsAllPartsIgnoreCase;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
class FileSearchByPathPatternReusableFileVisitor extends SimpleFileVisitor<Path> {
    
    private List<String> foundItems;
    private Path root;
    private String[] searchedPath;
    private String[] currentPath;
    private String currentPathName;
    private String searchedPathFragment;
    private int searchedCounter;
    
    FileSearchByPathPatternReusableFileVisitor() {
    }
    
    FileSearchByPathPatternReusableFileVisitor useAgainWith(Path root, String[] searched, List<String> found) {
        this.foundItems = found;
        this.root = root;
        this.searchedPath = searched;
        return this;
    }
    
    void clear() {
        this.foundItems = null;
        this.root = null;
        this.searchedPath = null;
        this.searchedPathFragment = null;
        this.currentPath = null;
        this.currentPathName = null;
        this.searchedCounter = 0;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        this.currentPathName = this.extractRelativeFileName(file);
        this.currentPath = normalizePathFragmentsFrom(this.currentPathName);
        if ( this.searchedPath.length > this.currentPath.length ) {
            return CONTINUE;
        }
        
        this.searchedCounter = 0;
        for (int i = 0; i < this.currentPath.length; i++) {
            this.searchedPathFragment = this.searchedPath[this.searchedCounter];
            if ( this.searchedPathFragment.contains("-") ) {
                if ( containsAllPartsIgnoreCase(this.currentPath[i], this.searchedPathFragment) ) {
                    this.searchedCounter++;
                }
            } else {
                if ( containsIgnoreCase(this.currentPath[i], this.searchedPathFragment) ) {
                    this.searchedCounter++;
                } 
            }            
        }
        
        if ( this.searchedCounter == this.searchedPath.length ) {
            this.foundItems.add(this.currentPathName);
        }
        return CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc)
            throws IOException {
        return CONTINUE;
    }
    
    private String extractRelativeFileName(Path file) {
        return relativizeFileName(this.root, file);
    }
}
