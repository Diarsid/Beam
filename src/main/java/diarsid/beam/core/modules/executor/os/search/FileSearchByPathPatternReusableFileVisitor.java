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
    public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs)
            throws IOException {
        return this.processItem(file);
    }        

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        return this.processItem(file);
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc)
            throws IOException {
        return CONTINUE;
    }
    
    private FileVisitResult processItem(Path file) {        
        this.currentPathName = this.extractRelativeFileName(file);
        if ( containsIgnoreCase(this.currentPathName, "desktop.ini") ) {
            return CONTINUE;
        }
        
        this.currentPath = normalizePathFragmentsFrom(this.currentPathName);
        if ( this.searchedPath.length > this.currentPath.length ) {
            return CONTINUE;
        }
        
        this.searchedCounter = 0;
        for (String currentPathFragment : this.currentPath) {            
            this.searchedPathFragment = this.searchedPath[this.searchedCounter];
            if ( this.searchedPathFragment.contains("-") ) {
                if ( containsAllPartsIgnoreCase(currentPathFragment, this.searchedPathFragment) ) {
                    this.searchedCounter++;
                }
            } else {
                if ( containsIgnoreCase(currentPathFragment, this.searchedPathFragment) ) {
                    this.searchedCounter++;
                } 
            }     
            if ( this.searchedCounter == this.searchedPath.length ) {
                if ( this.arraysEndsAreSimilar(this.currentPath, this.searchedPath)) {                    
                    break;
                } else {
                    this.searchedCounter = -1;
                    break;
                }                
            }
        }
        
        if ( this.searchedCounter == this.searchedPath.length ) {
            this.foundItems.add(this.currentPathName);
        } 
        return CONTINUE;
    }
    
    private boolean arraysEndsAreSimilar(
            String[] arrayWithFullNames, String[] arrayWithPartialNames) {
        return containsAllPartsIgnoreCase(
                arrayWithFullNames[arrayWithFullNames.length - 1], 
                arrayWithPartialNames[arrayWithPartialNames.length - 1]);
    }
    
    private String extractRelativeFileName(Path file) {
        return relativizeFileName(this.root, file);
    }
}
