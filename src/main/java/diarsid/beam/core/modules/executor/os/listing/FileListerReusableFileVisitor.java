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
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SIBLINGS;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import static diarsid.beam.core.modules.executor.os.listing.ProgramFolderDetector.PROGRAM_FOLDER;

/**
 *
 * @author Diarsid
 */
public class FileListerReusableFileVisitor extends SimpleFileVisitor<Path> {
    
    private final ProgramFolderDetector programFolderDetector;
    private final LargeFolderDetector largeFolderDetector;
    private final FileItemsFormatter formatter;
    
    private Path root;
    
    public FileListerReusableFileVisitor(
            ProgramFolderDetector programFolderDetector, 
            LargeFolderDetector largeFolderDetector,
            FileItemsFormatter formatter) {
        this.programFolderDetector = programFolderDetector;
        this.largeFolderDetector = largeFolderDetector;
        this.formatter = formatter;
    }
    
    void useAgainWith(Path root) {
        this.root = root;
    }
    
    void clear() {
        this.root = null;
        this.formatter.clear();
    }
    
    List<String> getResults() {
        return this.formatter.getResults();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
            throws IOException {
        int folderType = this.programFolderDetector.examineTypeOf(dir);
        if ( folderType == PROGRAM_FOLDER ) {
            this.formatter.skipFolderWithMessage(dir, "...program folder");
            return SKIP_SIBLINGS;
        }
        if ( this.largeFolderDetector.examine(dir) ) {
            this.formatter.skipFolderWithMessage(dir, "...folder too large");
            return SKIP_SUBTREE;
        }
        this.formatter.includeItem(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        this.formatter.includeItem(file);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        this.formatter.skipFailedItem(file, "access denied");
        return CONTINUE;
    }
}
