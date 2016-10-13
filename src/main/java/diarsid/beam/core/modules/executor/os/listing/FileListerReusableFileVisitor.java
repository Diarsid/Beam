/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.listing;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.SKIP_SIBLINGS;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 *
 * @author Diarsid
 */
public class FileListerReusableFileVisitor extends SimpleFileVisitor<Path> {
    
    private final ProgramFolderDetector programFolderDetector;
    private final LargeFolderDetector largeFolderDetector;
    private final ResultsFormatter programFolderFormatter;
    private final ResultsFormatter largeFolderFormatter;
    
    private List<String> result;
    private Path root;
    
    public FileListerReusableFileVisitor(
            ProgramFolderDetector programFolderDetector, 
            LargeFolderDetector largeFolderDetector) {
        this.programFolderDetector = programFolderDetector;
        this.largeFolderDetector = largeFolderDetector;
    }
    
    void useAgainWith(Path root) {
        this.result = new ArrayList<>();
        this.root = root;
    }
    
    void clear() {
        this.result = null;
        this.root = null;
    }
    
    List<String> getResults() {
        return this.result;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if ( this.programFolderDetector.examine(dir) ) {
            this.programFolderFormatter.formatResults(this.result);
            return SKIP_SIBLINGS;
        }
        if ( this.largeFolderDetector.examine(dir) ) {
            this.largeFolderFormatter.formatResults(this.result);
            return SKIP_SUBTREE;
        }
        
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
