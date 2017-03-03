/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.listing;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.domain.entities.Location;

import static java.nio.file.Files.walkFileTree;
import static java.util.EnumSet.of;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static diarsid.beam.core.base.util.Logs.logError;

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
        FileItemAnalizer analizer = new FileItemAnalizer();
        FolderTypeDetector programDetector = new FolderTypeDetector(analizer);
        FileListerReusableFileVisitor reusableFileVisitor = new FileListerReusableFileVisitor(
                programDetector, largeDetector, formatter);
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
            return of(this.visitor.getResults());
        } catch (IOException e) {
            logError(this.getClass(), "", e);
            //debug("[FILE LISTER] IOException while processing " + root.toString());
            return empty();
        } finally {
            this.visitor.clear();
        }       
    }
}
