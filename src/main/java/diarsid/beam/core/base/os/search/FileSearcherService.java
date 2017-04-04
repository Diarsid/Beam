/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.search;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import diarsid.beam.core.base.os.search.result.FileSearchResult;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.hasWildcard;
import static diarsid.beam.core.base.os.search.ItemType.typeOf;
import static diarsid.beam.core.base.os.search.result.FileSearchFailureImpl.invalidLocationFailure;
import static diarsid.beam.core.base.os.search.result.FileSearchFailureImpl.targetInvalidMessage;
import static diarsid.beam.core.base.os.search.result.FileSearchFailureImpl.targetNotFoundFailure;
import static diarsid.beam.core.base.os.search.result.FileSearchResultImpl.failWith;
import static diarsid.beam.core.base.os.search.result.FileSearchResultImpl.successWith;
import static diarsid.beam.core.base.os.search.result.FileSearchSuccessImpl.foundFile;
import static diarsid.beam.core.base.os.search.result.FileSearchSuccessImpl.foundFiles;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.base.util.PathUtils.pathIsDirectory;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.PathUtils.splitToParts;
import static diarsid.beam.core.base.util.PathUtils.trimSeparators;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsAllPartsIgnoreCase;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;

/**
 *
 * @author Diarsid
 */
class FileSearcherService implements FileSearcher {  
    
    private final int nameSearchDepth;
    private final int pathSearchDepth;
    
    FileSearcherService(
            int depthOfSearchByName, 
            int depthOfSearchByPath) {
        this.nameSearchDepth = depthOfSearchByName;
        this.pathSearchDepth = depthOfSearchByPath;
    }

    @Override
    public FileSearchResult findStrictly(
            String strictTarget, String location, FileSearchMode mode) {
        return this.findByStrictNameRecursively(strictTarget, Paths.get(location), mode);
    }

    @Override
    public FileSearchResult findStrictly(
            String strictTarget, Path location, FileSearchMode mode) {
        return this.findByStrictNameRecursively(strictTarget, location, mode);
    }
    
    private FileSearchResult findByStrictNameRecursively(
            String strictTarget, Path dir, FileSearchMode mode) {
        strictTarget = trimSeparators(strictTarget);
        if ( pathIsDirectory(dir) ) {
            if ( this.isAppropriatePath(dir, strictTarget, mode) ) {
                debug("[FILE SEARCHER] target found directly. No search.");
                return successWith(foundFile(strictTarget));
            } else {
                debug("[FILE SEARCHER] target not found directly. Search begins...");
                return this.searchStrictly(dir, strictTarget, mode);
            }            
        } else {
            return failWith(invalidLocationFailure());
        }
    }
    
    private FileSearchResult searchStrictly(Path root, String target, FileSearchMode mode) {
        List<String> foundItems;
        try { 
            
            debug("[FILE SEARCHER] ...search by strict name...");
            foundItems = this.collectItemsByStrictName(root, target, mode);
            
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
    
    private List<String> collectItemsByStrictName(
            Path root, String nameToFind, FileSearchMode mode) 
            throws IOException {
        return Files
                    .walk(root, this.nameSearchDepth, FOLLOW_LINKS)
                    .filter(path -> mode.correspondsTo(path))
                    .map(path -> root.relativize(path))
                    .filter(path -> this.filterSystemFiles(path))
                    .filter(path -> this.filterByStrictName(nameToFind, path))
                    .map(path -> normalizeSeparators(path.toString()))
                    .collect(toList());
    }
    
    private boolean filterByStrictName(String nameToFind, Path testedPath) {
        return asName(testedPath).equalsIgnoreCase(nameToFind);
    }

    @Override
    public FileSearchResult findDirectly(
            String strictTarget, String location, FileSearchMode mode) {
        debug("[FILE SEARCHER] must find strictly: ");
        debug("[FILE SEARCHER]    location : " + location);
        debug("[FILE SEARCHER]    target   : " + strictTarget);
        return this.findByDirectName(strictTarget, Paths.get(location), mode);
    }

    @Override
    public FileSearchResult findDirectly(
            String strictTarget, Path location, FileSearchMode mode) {
        debug("[FILE SEARCHER] must find strictly: ");
        debug("[FILE SEARCHER]    location : " + location.toString());
        debug("[FILE SEARCHER]    target   : " + strictTarget);
        return this.findByDirectName(strictTarget, location, mode);
    }
    
    private FileSearchResult findByDirectName(String target, Path location, FileSearchMode mode) {
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

    private FileSearchResult findByNameOrByPatternMatch(
            Path dir, String target, FileSearchMode mode) {        
        target = trimSeparators(target);
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
        List<String> foundItems;
        try {             
            
            if ( containsPathSeparator(target) ) {
                debug("[FILE SEARCHER] ...search by path...");
                foundItems = this.collectItemsByPathParts(root, target, mode);
            } else {
                debug("[FILE SEARCHER] ...search by name...");
                foundItems = this.collectItemsByNamePattern(root, target, mode);
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

    private List<String> collectItemsByNamePattern(
            Path root, String nameToFind, FileSearchMode mode)
            throws IOException {
        
        if ( hasWildcard(nameToFind) ) {
            List<String> patterns = splitByWildcard(nameToFind);
            
            return Files
                    .walk(root, this.nameSearchDepth, FOLLOW_LINKS)
                    .filter(path -> mode.correspondsTo(path))
                    .map(path -> root.relativize(path))
                    .filter(path -> this.filterSystemFiles(path))
                    .filter(path -> this.filterByNamePatterns(patterns, path))
                    .map(path -> normalizeSeparators(path.toString()))
                    .collect(toList());
            
        } else {
            return Files
                    .walk(root, this.nameSearchDepth, FOLLOW_LINKS)
                    .filter(path -> mode.correspondsTo(path))
                    .map(path -> root.relativize(path))
                    .filter(path -> this.filterSystemFiles(path))
                    .filter(path -> this.filterByNamePattern(nameToFind, path))
                    .map(path -> normalizeSeparators(path.toString()))
                    .collect(toList());
        }
    }
    
    private boolean filterSystemFiles(Path path) {
        return ! containsIgnoreCase(asName(path), "desktop.ini");
    }
    
    private boolean filterByNamePattern(String nameToFind, Path testedPath) {
        return containsIgnoreCase(asName(testedPath), nameToFind);
    }
    
    private boolean filterByNamePatterns(List<String> patterns, Path testedPath) {
        return containsAllPartsIgnoreCase(asName(testedPath), patterns);
    }
    
    private List<String> collectItemsByPathParts(
            Path root, String target, FileSearchMode mode) 
            throws IOException {
        
        String[] targetPathParts = splitPathFragmentsFrom(target);
        /*
         * Following variables are transmitted into method that is being 
         * invoked for many times during stream operation.
         * 
         * They are required by the algorithm inside the method. If they 
         * have been declared inside of the method scope, it would entail 
         * the unneccesary spawning of garbage variables inside of the 
         * method body due to a lot of method's invocations. 
         * 
         * Thery are transmitted to method as parameters in order to provide 
         * its algorithm with required variables and avoid unneccesary 
         * garbage creation.
         */
        int counter = 0;
        String searchedPart = "";
        
        return Files
                .walk(root, this.pathSearchDepth, FOLLOW_LINKS)
                .filter(path -> mode.correspondsTo(path))
                .map(path -> root.relativize(path))
                .filter(path -> this.filterSystemFiles(path))
                .filter(path -> this.filterByPathParts(
                        counter, searchedPart, splitToParts(path), targetPathParts))
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }
    
    private boolean filterByPathParts(
            int counter, String searchedPart, String[] realPathParts, String[] searchedPathParts) {
        if ( realPathParts.length == 0 ) {
            return false;
        }
        if ( realPathParts.length < searchedPathParts.length ) {
            return false;
        }
        
        for (String realPart : realPathParts) {
            if ( counter == searchedPathParts.length ) {
                break;
            }
            searchedPart = searchedPathParts[counter];
            if ( hasWildcard(searchedPart) ) {
                if ( containsAllPartsIgnoreCase(realPart, searchedPart) ) {
                    counter++;
                }
            } else {
                if ( containsIgnoreCase(realPart, searchedPart) ) {
                    counter++;
                }
            }
        }
        
        return counter == searchedPathParts.length;
    }

    private String ioExceptionMessageFor(String nameToFind, Path root) {
        return "java.nio.file.Files.walkFileTree() -> Unknown IOExceoption with " + 
                nameToFind + " in " + root.toString();
    }

    private String accessDeniedMessageFor(String nameToFind, Path root) {
        return "Access denied to " + nameToFind + " in " + root.toString();
    }
}
