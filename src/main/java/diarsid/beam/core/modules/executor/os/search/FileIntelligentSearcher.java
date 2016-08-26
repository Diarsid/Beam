/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitOption;
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
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchFailureImpl.invalidLocationFailure;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchFailureImpl.targetInvalidMessage;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchFailureImpl.targetNotFoundFailure;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchResultImpl.failWith;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchResultImpl.successWith;
import static diarsid.beam.core.modules.executor.os.search.result.FileSearchSuccessImpl.foundFiles;
import static diarsid.beam.core.util.Logs.logError;

/**
 *
 * @author Diarsid
 */
class FileIntelligentSearcher implements FileSearcher {  
    
    private final int deep;
    private final FileSearchByNamePatternReusableFileVisitor reusableVisitorByName;
    private final FileSearchByPathPatternReusableFileVisitor reusableVisitorByPath;
    
    FileIntelligentSearcher(
            int deep, 
            FileSearchByNamePatternReusableFileVisitor visitorByName, 
            FileSearchByPathPatternReusableFileVisitor visitorByPath) {
        this.deep = deep;
        this.reusableVisitorByName = visitorByName;
        this.reusableVisitorByPath = visitorByPath;
    }
    
    @Override
    public FileSearchResult findTarget(String target, String location) {
        Path dir = Paths.get(location);        
        if ( givenPathIsDirectory(dir) ) {
            return search(dir, target);
        } else {
            return failWith(invalidLocationFailure());
        }
    }
    
    private FileSearchResult search(Path root, String target) {
        List<String> foundItems = new ArrayList<>();
        try {             
            
            if ( containsFileSeparator(target) ) {
                this.collectFoundFilesByPathParts(
                        root, normalizePathFragmentsFrom(target), foundItems);
            } else {
                this.collectFoundFilesByNameInRoot(
                        root, target, foundItems);
            }            
            
            if ( foundItems.isEmpty() ) {
                return failWith(targetNotFoundFailure());
            } else {
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
            Path root, String nameToFind, List<String> foundItems)
            throws IOException {
        walkFileTree(
                root,
                EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                this.deep,
                this.reusableVisitorByName.useAgainWith(root, nameToFind, foundItems));
        foundItems.remove("");        
        this.reusableVisitorByName.clear();
    }
    
    private void collectFoundFilesByPathParts(
            Path root, String[] targetPathParts, List<String> foundItems) 
            throws IOException {
        walkFileTree(
                root, 
                this.reusableVisitorByPath.useAgainWith(root, targetPathParts, foundItems));
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
