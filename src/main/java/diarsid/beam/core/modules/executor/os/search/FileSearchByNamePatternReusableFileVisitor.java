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

import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.relativizeFileName;
import static diarsid.beam.core.modules.executor.os.search.ItemType.FILE;
import static diarsid.beam.core.modules.executor.os.search.ItemType.FOLDER;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.splitByDash;

/**
 *
 * @author Diarsid
 */
public class FileSearchByNamePatternReusableFileVisitor extends SimpleFileVisitor<Path> {
        
    private Path root;
    private String nameToFind;
    private FileSearchMode searchMode;
    private List<String> foundItems;
    
    public FileSearchByNamePatternReusableFileVisitor() {
    }
    
    public FileSearchByNamePatternReusableFileVisitor useAgainWith(
            Path root, String nameToFind, List<String> foundItems, FileSearchMode mode) {
        this.root = root;
        this.nameToFind = nameToFind;
        this.foundItems = foundItems;
        this.searchMode = mode;
        return this;
    }
    
    public void clear() {
        this.searchMode = null;
        this.root = null;
        this.nameToFind = null;
        this.foundItems = null;
    }
    
    @Override 
    public FileVisitResult preVisitDirectory(Path file, BasicFileAttributes attrs)
            throws IOException {
        return this.processItem(file, FOLDER);
    }        

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        return this.processItem(file, FILE);
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc)
            throws IOException {        
        return CONTINUE;
    }
    
    private FileVisitResult processItem(Path file, ItemType type) {        
        if ( ! this.searchMode.correspondsTo(type) ) {
            return CONTINUE;
        }
        
        String fileName;
        if ( file.getNameCount() > 0 ) {
            fileName = file.getFileName().toString();
        } else {
            fileName = file.toString();
        }
        if ( containsIgnoreCase(fileName, "desktop.ini") ) {
            return CONTINUE;
        }
        
        if ( this.nameToFind.contains("-") ) {
            for (String fragment : splitByDash(this.nameToFind)) {
                if ( ! containsIgnoreCase(fileName, fragment) ) {
                    return CONTINUE;
                }                
            }
            this.foundItems.add(this.extractRelativeFileName(file));
            return CONTINUE;
        } else {
            if ( containsIgnoreCase(fileName, this.nameToFind) ) {
                this.foundItems.add(this.extractRelativeFileName(file));
            }
        }
        return CONTINUE;
    }

    private String extractRelativeFileName(Path file) {
        return relativizeFileName(this.root, file);
    }
}
