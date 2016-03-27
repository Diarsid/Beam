/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import diarsid.beam.core.modules.IoInnerModule;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 *
 * @author Diarsid
 */
class SearchFileVisitor extends SimpleFileVisitor<Path> {
    
    private final IoInnerModule ioEngine;
    
    private Path root;
    private String nameToFind;
    private List<String> foundItems;
    
    SearchFileVisitor(IoInnerModule io) {
        this.ioEngine = io;
    }
    
    SearchFileVisitor set(Path root, String nameToFind, List<String> foundItems) {
        this.root = root;
        this.nameToFind = nameToFind;
        this.foundItems = foundItems;
        return this;
    }
    
    void clear() {
        this.root = null;
        this.nameToFind = null;
        this.foundItems = null;
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
        
        this.ioEngine.reportError("Unable to process " + 
                file.normalize().toString().replace("\\", "/"));
        
        return CONTINUE;
    }
    
    private FileVisitResult processItem(Path file) {
        
        String fileName;
        if ( file.getNameCount() > 0 ) {
            fileName = file.getFileName().toString().toLowerCase();
        } else {
            fileName = file.toString();
        }
        if ( fileName.contains("desktop.ini") ) {
            return CONTINUE;
        }
        
        if ( this.nameToFind.contains("-") ) {
            for (String fragment : Arrays.asList(this.nameToFind.split("-"))) {
                if ( !fileName.contains(fragment) ) {
                    return CONTINUE;
                }                
            }
            this.foundItems.add(this.root
                            .relativize(file)
                            .toString()
                            .replace("\\", "/"));
            return CONTINUE;
        } else {
            if ( fileName.contains(this.nameToFind) ) {
                this.foundItems.add(this.root
                        .relativize(file)
                        .toString()
                        .replace("\\", "/"));
            }
        }

        return CONTINUE;
    }
}
