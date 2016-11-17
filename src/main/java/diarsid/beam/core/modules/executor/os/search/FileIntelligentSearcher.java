/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import diarsid.beam.core.modules.executor.os.search.result.FileSearchResult;

import static java.nio.file.Files.walkFileTree;

import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.containsFileSeparator;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.givenPathIsDirectory;
import static diarsid.beam.core.modules.executor.os.search.FileSearchUtils.normalizePathFragmentsFrom;
import static diarsid.beam.core.modules.executor.os.search.ItemType.typeOf;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchFailureImpl.invalidLocationFailure;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchFailureImpl.targetInvalidMessage;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchFailureImpl.targetNotFoundFailure;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchResultImpl.failWith;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchResultImpl.successWith;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchSuccessImpl.foundFile;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchSuccessImpl.foundFiles;
import static diarsid.beam.core.util.Logs.debug;
import static diarsid.beam.core.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
class FileIntelligentSearcher implements FileSearcher {  
    
    private final int nameSearchDepth;
    private final int pathSearchDepth;
    private final FileSearchByNamePatternReusableFileVisitor reusableVisitorByName;
    private final FileSearchByPathPatternReusableFileVisitor reusableVisitorByPath;
    
    FileIntelligentSearcher(
            int depthOfSearchByName, 
            int depthOfSearchByPath,
            FileSearchByNamePatternReusableFileVisitor visitorByName, 
            FileSearchByPathPatternReusableFileVisitor visitorByPath) {
        this.nameSearchDepth = depthOfSearchByName;
        this.pathSearchDepth = depthOfSearchByPath;
        this.reusableVisitorByName = visitorByName;
        this.reusableVisitorByPath = visitorByPath;
    }
    
    @Override
    public FileSearchResult findTarget(String target, String location, FileSearchMode mode) {
        debug("[FILE SEARCHER] must find: ");
        debug("[FILE SEARCHER]    location : " + location);
        debug("[FILE SEARCHER]    target   : " + target);
        Path dir = Paths.get(location);        
        if ( givenPathIsDirectory(dir) ) {
            if ( this.isAppropriatePath(location, target, mode) ) {
                debug("[FILE SEARCHER] target found directly. No search.");
                return successWith(foundFile(target) );
            } else {
                debug("[FILE SEARCHER] target not found directly. Search begins...");
                return this.search(dir, target, mode);
            }            
        } else {
            return failWith(invalidLocationFailure());
        }
    }

    private boolean isAppropriatePath(String location, String target, FileSearchMode mode) {
        Path fullPath = Paths.get(location + "/" + target);
        return ( Files.exists(fullPath) && mode.correspondsTo(typeOf(fullPath)) );
    }
    
    private FileSearchResult search(Path root, String target, FileSearchMode mode) {
        List<String> foundItems = new ArrayList<>();
        try {             
            
            if ( containsFileSeparator(target) ) {
                debug("[FILE SEARCHER] ...search by path...");
                this.collectFoundFilesByPathParts(
                        root, normalizePathFragmentsFrom(target), foundItems, mode);
            } else {
                debug("[FILE SEARCHER] ...search by name...");
                this.collectFoundFilesByNameInRoot(
                        root, target, foundItems, mode);
            }            
            
            if ( foundItems.isEmpty() ) {
                debug("[FILE SEARCHER] not found.");
                return failWith(targetNotFoundFailure());
            } else {
                debug("[FILE SEARCHER] found : " + foundItems);
                return successWith(foundFiles(foundItems));
            }            
        } catch (AccessDeniedException e ) {
            logError(this.getClass(), this.accessDeniedMessageFor(target, root), e);
            return failWith(targetInvalidMessage(this.accessDeniedMessageFor(target, root)));
        } catch (IOException e ) {
            logError(this.getClass(), this.ioExceptionMessageFor(target, root), e);
            return failWith(targetInvalidMessage("Unknown IOException occured."));
        }
    }

    private void collectFoundFilesByNameInRoot(
            Path root, String nameToFind, List<String> foundItems, FileSearchMode mode)
            throws IOException {
        walkFileTree(
                root,
                EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                this.nameSearchDepth,
                this.reusableVisitorByName
                        .useAgainWith(root, nameToFind, foundItems, mode));
        foundItems.remove("");        
        this.reusableVisitorByName.clear();
    }
    
    private void collectFoundFilesByPathParts(
            Path root, String[] targetPathParts, List<String> foundItems, FileSearchMode mode) 
            throws IOException {
        walkFileTree(
                root, 
                EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                this.pathSearchDepth,
                this.reusableVisitorByPath
                        .useAgainWith(root, targetPathParts, foundItems, mode));
        foundItems.remove("");        
        this.reusableVisitorByPath.clear();
    }

    private String ioExceptionMessageFor(String nameToFind, Path root) {
        return "java.nio.file.Files.walkFileTree() -> Unknown IOExceoption with " + 
                nameToFind + " in " + root.toString();
    }

    private String accessDeniedMessageFor(String nameToFind, Path root) {
        return "Access denied to " + nameToFind + " in " + root.toString();
    }
}
