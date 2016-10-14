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
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import static diarsid.beam.core.modules.executor.os.listing.FileItemsFormatter.INLINE_SKIPPED;
import static diarsid.beam.core.modules.executor.os.listing.FileItemsFormatter.NEW_LINE_SKIPPED;
import static diarsid.beam.core.util.Logs.debug;
import static diarsid.beam.core.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
public class FileListerReusableFileVisitor extends SimpleFileVisitor<Path> {
    
    private final FolderTypeDetector programFolderDetector;
    private final LargeFolderDetector largeFolderDetector;
    private final FileItemsFormatter formatter;
    
    private Path root;
    
    public FileListerReusableFileVisitor(
            FolderTypeDetector programFolderDetector, 
            LargeFolderDetector largeFolderDetector,
            FileItemsFormatter formatter) {
        this.programFolderDetector = programFolderDetector;
        this.largeFolderDetector = largeFolderDetector;
        this.formatter = formatter;
    }
    
    void useAgainWith(Path root) {
        this.formatter.presetWith(root);
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
        try {
            debug("[FILE LIST VISITOR] pre-visit dir: " + dir.getFileName().toString());
        } catch (NullPointerException npe) {
            debug("[FILE LIST VISITOR] pre-visit dir: " + dir);
        }
        
        FolderType type = this.programFolderDetector.examineTypeOf(dir);
        switch (type) {
            case PROGRAM_FOLDER : {
                this.formatter.skipFolderWithMessage(dir, "(program)", INLINE_SKIPPED);
                debug("[FILE LIST VISITOR] program folder!");
                return SKIP_SUBTREE;
            }
            case RESTRICTED_FOLDER : {
                return SKIP_SUBTREE;
            }
            case PROJECT_FOLDER : {
                this.formatter.skipFolderWithMessage(dir, "(project)", INLINE_SKIPPED);
                debug("[FILE LIST VISITOR] project folder!");
                return SKIP_SUBTREE;
            }
            default : {
                // do nothing.
            }
        }
        if ( ! dir.equals(this.root) && this.largeFolderDetector.examine(dir) ) {
            this.formatter.skipFolderWithMessage(dir, " ...too large", NEW_LINE_SKIPPED);
            debug("[FILE LIST VISITOR] too large, skip subtree");
            return SKIP_SUBTREE;
        }
        debug("[FILE LIST VISITOR] include: " + dir);
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
        logError(this.getClass(),"", exc);
        this.formatter.skipFailedItem(file, "access denied");
        return CONTINUE;
    }
}
