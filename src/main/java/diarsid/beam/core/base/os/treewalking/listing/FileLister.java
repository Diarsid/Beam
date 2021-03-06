/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.treewalking.listing;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector;
import diarsid.beam.core.domain.entities.Location;

import static java.nio.file.Files.walkFileTree;
import static java.util.EnumSet.of;

import static diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector.getFolderTypeDetector;
import static diarsid.support.log.Logging.logFor;

/**
 *
 * @author Diarsid
 */
public class FileLister {
    
    private final FileListerReusableFileVisitor visitor;
    
    public FileLister(FileListerReusableFileVisitor lister) {
        this.visitor = lister;
    }
    
    public static FileLister getLister() {
        FileItemsFormatter formatter = new FileItemsFormatter();
        LargeFolderDetector largeDetector = new LargeFolderDetector(20);
        FolderTypeDetector folderTypeDetector = getFolderTypeDetector();
        FileListerReusableFileVisitor reusableFileVisitor = new FileListerReusableFileVisitor(
                folderTypeDetector, largeDetector, formatter);
        return new FileLister(reusableFileVisitor);
    }
    
    public Optional<List<String>> listContentOf(Location location, int depth) {
        return this.list(Paths.get(location.path()), depth);
    }
    
    public Optional<List<String>> listContentOf(Path root, int depth) {      
        return this.list(root, depth);
    }
    
    private Optional<List<String>> list(Path root, int depth) {        
        this.visitor.useAgainWith(root);
        try {
            walkFileTree(root, of(FileVisitOption.FOLLOW_LINKS), depth, this.visitor);
            return Optional.of(this.visitor.getResults());
        } catch (IOException e) {
            logFor(this).error(e.getMessage(), e);
            return Optional.empty();
        } finally {
            this.visitor.clear();
        }       
    }
}
