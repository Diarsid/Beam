/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.treewalking.search;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult;
import diarsid.beam.core.base.util.Pair;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.os.treewalking.search.ItemType.typeOf;
import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult.failWithInvalidLocationFailure;
import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult.failWithTargetInvalidMessage;
import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult.failWithTargetNotFoundFailure;
import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult.successWithFile;
import static diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult.successWithFiles;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.PathUtils.combineAsPathFrom;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.base.util.PathUtils.toSubpathAndTarget;
import static diarsid.beam.core.base.util.PathUtils.trimSeparators;

/**
 *
 * @author Diarsid
 */
class FileSearcherService implements FileSearcher {  
    
    private final FilesCollector filesCollector;
    
    FileSearcherService(FilesCollector filesCollector) {
        this.filesCollector = filesCollector;
    }

    @Override
    public FileSearchResult find(
            String target, String location, FileSearchMatching matching, FileSearchMode mode) {
        return this.doSearch(target, Paths.get(location), matching, mode);
    }

    @Override
    public FileSearchResult find(
            String target, Path location, FileSearchMatching matching, FileSearchMode mode) {
        return this.doSearch(target, location, matching, mode);
    }

    @Override
    public FileSearchResult findAll(Path location, FileSearchMode mode) {
        try {
            if ( pathIsDirectory(location) ) {
                List<String> foundItems = this.filesCollector.collectAll(location, mode);
                if ( foundItems.isEmpty() ) {
                    return failWithTargetNotFoundFailure();
                } else {
                    return successWithFiles(foundItems);
                }            
            } else {
                return failWithInvalidLocationFailure();
            }
        } catch (AccessDeniedException e) {
            String message = this.accessDeniedMessageFor(location);
            logError(this.getClass(), message, e);
            return failWithTargetInvalidMessage(message);
        } catch (IOException e) {
            String message = this.ioExceptionMessageFor(location);
            logError(this.getClass(), message, e);
            return failWithTargetInvalidMessage(message);
        }        
    }
    
    private FileSearchResult doSearch(
            String target, Path location, FileSearchMatching matching, FileSearchMode mode) {
        try {
            switch ( matching ) {
                case DIRECT_MATCH : {
                    return this.findByDirectName(target, location, mode);
                }    
                case STRICT_MATCH : {
                    return this.findByDirectNameOrByStrictName(target, location, mode);
                }    
                case PATTERN_MATCH : {
                    return this.findByDirectNameOrByWholeNamePattern(target, location, mode);
                }    
                case SIMILAR_MATCH : {
                    return this.findByDirectNameOrByNamePatternSimilarity(target, location, mode);
                }    
                default : {
                    return failWithTargetNotFoundFailure();
                }    
            }
        } catch (AccessDeniedException e) {
            logError(this.getClass(), this.accessDeniedMessageFor(target, location), e);
            return failWithTargetInvalidMessage(this.accessDeniedMessageFor(target, location));
        } catch (IOException e) {
            logError(this.getClass(), this.ioExceptionMessageFor(target, location), e);
            return failWithTargetInvalidMessage("Unknown IOException occured.");
        }
    }
    
    private FileSearchResult findByDirectNameOrByStrictName(
            String strictTarget, Path dir, FileSearchMode mode) 
            throws AccessDeniedException, IOException {
        strictTarget = trimSeparators(strictTarget);
        if ( pathIsDirectory(dir) ) {
            if ( this.isAppropriatePath(dir, strictTarget, mode) ) {
                return successWithFile(strictTarget);
            } else {
                List<String> foundItems;
                if ( containsPathSeparator(strictTarget) ) {
                    Pair<String, String> subpathTarget = toSubpathAndTarget(strictTarget);
                    dir = dir.resolve(subpathTarget.first());
                    strictTarget = subpathTarget.second();
                    foundItems = this.filesCollector.collectByStrictName(dir, strictTarget, mode);
                    foundItems = foundItems
                            .stream()
                            .map(foundItem -> combineAsPathFrom(subpathTarget.first(), foundItem))
                            .collect(toList());
                } else {
                    foundItems = this.filesCollector.collectByStrictName(dir, strictTarget, mode);
                }
                
                if ( foundItems.isEmpty() ) {
                    return failWithTargetNotFoundFailure();
                } else {
                    return successWithFiles(foundItems);
                }
            }            
        } else {
            return failWithInvalidLocationFailure();
        }
    }
    
    private FileSearchResult findByDirectName(
            String directTarget, Path location, FileSearchMode mode) {
        if ( pathIsDirectory(location) ) {
            if ( this.isAppropriatePath(location, directTarget, mode) ) {
                return successWithFile(directTarget);
            } else {
                return failWithTargetNotFoundFailure();
            }
        } else {
            return failWithInvalidLocationFailure();
        }
    }
    
    private FileSearchResult findByDirectNameOrByWholeNamePattern(
            String target, Path dir, FileSearchMode mode) 
            throws AccessDeniedException, IOException {
        target = trimSeparators(target);
        if ( pathIsDirectory(dir) ) {
            if ( this.isAppropriatePath(dir, target, mode) ) {
                return successWithFile(target);
            } else {
                return this.searchByWholeNamePattern(dir, target, mode);
            }
        } else {
            return failWithInvalidLocationFailure();
        }
    }

    private FileSearchResult findByDirectNameOrByNamePatternSimilarity(
            String target, Path dir, FileSearchMode mode) 
            throws AccessDeniedException, IOException {        
        target = trimSeparators(target);
        if ( pathIsDirectory(dir) ) {
            if ( this.isAppropriatePath(dir, target, mode) ) {
                return successWithFile(target);
            } else {
                return this.searchBySimilarity(dir, target, mode);
            }            
        } else {
            return failWithInvalidLocationFailure();
        }
    }
    
    private FileSearchResult searchByWholeNamePattern(Path dir, String target, FileSearchMode mode) 
            throws AccessDeniedException, IOException {
        List<String> foundItems = this.filesCollector.collectByWholeNamePattern(dir, target, mode);
        if ( foundItems.isEmpty() ) {
            return failWithTargetNotFoundFailure();
        } else {
            return successWithFiles(foundItems);
        }
    }
    
    private boolean isAppropriatePath(Path dir, String target, FileSearchMode mode) {
        Path fullPath = dir.resolve(target);
        return ( Files.exists(fullPath) && mode.correspondsTo(typeOf(fullPath)) );
    }
    
    private FileSearchResult searchBySimilarity(Path root, String target, FileSearchMode mode) 
            throws AccessDeniedException, IOException {
        List<String> foundItems;
        boolean containsPathSeparators = containsPathSeparator(target);
        if ( containsPathSeparators ) {
            foundItems = this.filesCollector.collectByPathPartsSimilarity(root, target, mode);
        } else {
            foundItems = this.filesCollector.collectByNamePatternSimilarity(root, target, mode);
        }            

        if ( foundItems.isEmpty() ) {
            if ( ! containsPathSeparators ) {
                foundItems = this.filesCollector
                        .collectBySubpathPatternSimilarityIgnoreSeparators(root, target, mode);
                if ( foundItems.isEmpty() ) {
                    return failWithTargetNotFoundFailure();
                } else {
                    return successWithFiles(foundItems);
                } 
            } else {
                return failWithTargetNotFoundFailure();
            }                      
        } else {
            return successWithFiles(foundItems);
        }
    }    

    private String ioExceptionMessageFor(String nameToFind, Path root) {
        return "java.nio.file.Files.walkFileTree() -> Unknown IOExceoption with " + 
                nameToFind + " in " + root.toString();
    }
    
    private String ioExceptionMessageFor(Path root) {
        return "java.nio.file.Files.walkFileTree() -> Unknown IOExceoption with " + root.toString();
    }

    private String accessDeniedMessageFor(String nameToFind, Path root) {
        return "Access denied to " + nameToFind + " in " + root.toString();
    }
    
    private String accessDeniedMessageFor(Path root) {
        return "Access denied to " + root.toString();
    }
}
