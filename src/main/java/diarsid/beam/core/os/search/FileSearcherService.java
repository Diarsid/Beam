/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.os.search;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import diarsid.beam.core.os.search.result.FileSearchResult;

import static java.nio.file.Files.walkFileTree;

import static diarsid.beam.core.os.search.ItemType.typeOf;
import static diarsid.beam.core.os.search.result.FileSearchFailureImpl.invalidLocationFailure;
import static diarsid.beam.core.os.search.result.FileSearchFailureImpl.targetInvalidMessage;
import static diarsid.beam.core.os.search.result.FileSearchFailureImpl.targetNotFoundFailure;
import static diarsid.beam.core.os.search.result.FileSearchResultImpl.failWith;
import static diarsid.beam.core.os.search.result.FileSearchResultImpl.successWith;
import static diarsid.beam.core.os.search.result.FileSearchSuccessImpl.foundFile;
import static diarsid.beam.core.os.search.result.FileSearchSuccessImpl.foundFiles;
import static diarsid.beam.core.util.Logs.debug;
import static diarsid.beam.core.util.Logs.logError;
import static diarsid.beam.core.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.util.PathUtils.normalizePathFragmentsFrom;
import static diarsid.beam.core.util.PathUtils.pathIsDirectory;

/**
 *
 * @author Diarsid
 */
class FileSearcherService implements FileSearcher {  
    
    private final int nameSearchDepth;
    private final int pathSearchDepth;
    private final FileSearchByNamePatternReusableFileVisitor reusableVisitorByName;
    private final FileSearchByPathPatternReusableFileVisitor reusableVisitorByPath;
    
    FileSearcherService(
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
    public FileSearchResult findStrictly(
            String strictTarget, String location, FileSearchMode mode) {
        debug("[FILE SEARCHER] must find strictly: ");
        debug("[FILE SEARCHER]    location : " + location);
        debug("[FILE SEARCHER]    target   : " + strictTarget);
        return this.findByStrictName(strictTarget, Paths.get(location), mode);
    }

    @Override
    public FileSearchResult findStrictly(
            String strictTarget, Path location, FileSearchMode mode) {
        debug("[FILE SEARCHER] must find strictly: ");
        debug("[FILE SEARCHER]    location : " + location.toString());
        debug("[FILE SEARCHER]    target   : " + strictTarget);
        return this.findByStrictName(strictTarget, location, mode);
    }
    
    private FileSearchResult findByStrictName(String target, Path location, FileSearchMode mode) {
        if ( pathIsDirectory(location) ) {
            if ( this.isAppropriatePath(location, target, mode) ) {
                return successWith(foundFile(target));
            } else {
                return failWith(targetNotFoundFailure());
            }
        } else {
            return failWith(invalidLocationFailure());
        }
    }
    
    @Override
    public FileSearchResult find(String target, String location, FileSearchMode mode) {
        debug("[FILE SEARCHER] must find: ");
        debug("[FILE SEARCHER]    location : " + location);
        debug("[FILE SEARCHER]    target   : " + target);
        return this.findByNameOrByPatternMatch(Paths.get(location), target, mode);
    }

    @Override
    public FileSearchResult find(String target, Path location, FileSearchMode mode) {
        debug("[FILE SEARCHER] must find: ");
        debug("[FILE SEARCHER]    location : " + location.toString());
        debug("[FILE SEARCHER]    target   : " + target);
        return this.findByNameOrByPatternMatch(location, target, mode);
    }

    private FileSearchResult findByNameOrByPatternMatch(Path dir, String target, FileSearchMode mode) {
        if ( pathIsDirectory(dir) ) {
            if ( this.isAppropriatePath(dir, target, mode) ) {
                debug("[FILE SEARCHER] target found directly. No search.");
                return successWith(foundFile(target));
            } else {
                debug("[FILE SEARCHER] target not found directly. Search begins...");
                return this.search(dir, target, mode);
            }            
        } else {
            return failWith(invalidLocationFailure());
        }
    }
    
    private boolean isAppropriatePath(Path dir, String target, FileSearchMode mode) {
        Path fullPath = dir.resolve(target);
        return ( Files.exists(fullPath) && mode.correspondsTo(typeOf(fullPath)) );
    }
    
    private FileSearchResult search(Path root, String target, FileSearchMode mode) {
        List<String> foundItems = new ArrayList<>();
        try {             
            
            if ( containsPathSeparator(target) ) {
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
