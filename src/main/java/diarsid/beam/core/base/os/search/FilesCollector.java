/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.PathUtils.cleanSeparators;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.PathUtils.splitToParts;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.isSimilarIgnoreCase;

/**
 *
 * @author Diarsid
 */
class FilesCollector {
    
    private final int nameSearchDepth;
    private final int pathSearchDepth;

    FilesCollector(int depthOfSearchByName, int depthOfSearchByPath) {        
        this.nameSearchDepth = depthOfSearchByName;
        this.pathSearchDepth = depthOfSearchByPath;
    }
    
    private boolean filterByStrictName(String nameToFind, Path testedPath) {
        return asName(testedPath).equalsIgnoreCase(nameToFind);
    }
    
    private boolean filterSystemFiles(Path path) {
        return ! containsIgnoreCase(asName(path), "desktop.ini");
    }
    
    private boolean filterByNamePatternSimilarity(String nameToFind, String nameFromPath) {
        if ( containsIgnoreCase(nameFromPath, nameToFind) ) {
            return true;
        } else {
            boolean result = isSimilarIgnoreCase(nameFromPath, nameToFind);
            return result;
        }
    }
    
    private boolean filterByPathPartsSimilarity(
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
            if ( containsIgnoreCase(realPart, searchedPart) ) {
                counter++;
            } else if ( isSimilarIgnoreCase(realPart, searchedPart) ) {
                counter++;
            }
        }
        
        return counter == searchedPathParts.length;
    }

    private Stream<Path> prepareCollectingPathStream(Path root, FileSearchMode mode) throws IOException {        
        return Files
                .walk(root, this.nameSearchDepth, FOLLOW_LINKS)
                .filter(path -> mode.correspondsTo(path))
                .map(path -> root.relativize(path))
                .peek(path -> System.out.println(path.toString()))
                .filter(path -> this.filterSystemFiles(path));
    }
    
    List<String> collectByStrictName(
            Path root, String nameToFind, FileSearchMode mode) 
            throws IOException {
        return this.prepareCollectingPathStream(root, mode)
                .filter(path -> this.filterByStrictName(nameToFind, path))
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }

    List<String> collectByNamePatternSimilarity(
            Path root, String nameToFind, FileSearchMode mode)
            throws IOException {        
        return this.prepareCollectingPathStream(root, mode)
                .filter(path -> this.filterByNamePatternSimilarity(nameToFind, asName(path)))
                // TODO
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }
    
    List<String> collectBySubpathPatternSimilarityIgnoreSeparators(
            Path root, String nameToFind, FileSearchMode mode)
            throws IOException {        
        return this.prepareCollectingPathStream(root, mode)
                .map(path -> cleanSeparators(path.toString()))
                .filter(fileName -> this.filterByNamePatternSimilarity(nameToFind, fileName))
                .collect(toList());
    }
    
    List<String> collectByWholeNamePattern(
            Path root, String nameToFind, FileSearchMode mode)
            throws IOException {        
        return this.prepareCollectingPathStream(root, mode)
                .filter(path -> containsIgnoreCase(asName(path), nameToFind))
                .peek(path -> System.out.println("after contains(): " + path.getFileName().toString()))
                .map(path -> normalizeSeparators(path.toString()))
                .peek(path -> System.out.println("collecting: " + path))
                .collect(toList());
    }
    
    List<String> collectByPathPartsSimilarity(
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
                .filter(path -> this.filterByPathPartsSimilarity(
                        counter, searchedPart, splitToParts(path), targetPathParts))
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }
}
