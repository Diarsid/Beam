/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.PathUtils.normalizeSeparators;
import static diarsid.beam.core.base.util.PathUtils.removeSeparators;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;
import static diarsid.beam.core.base.util.PathUtils.splitToParts;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
class FilesCollectorByStream implements FilesCollector {
    
    private final int searchDepth;

    FilesCollectorByStream(int searchDepth) {        
        debug("[FILE SEARCH] by stream");
        this.searchDepth = searchDepth;
    }
    
    private boolean filterByStrictName(String nameToFind, Path testedPath) {
        return lower(asName(testedPath)).startsWith(lower(nameToFind));
    }
    
    private boolean filterSystemFiles(Path path) {
        return ! containsIgnoreCase(asName(path), "desktop.ini");
    }
    
    private boolean filterByNamePatternSimilarity(String nameToFind, String nameFromPath) {
        nameFromPath = removeSeparators(nameFromPath);
        if ( containsIgnoreCase(nameFromPath, nameToFind) ) {
            return true;
        } else {
            return isSimilar(nameFromPath, nameToFind);
        }
    }
    
    private boolean filterByPathPartsSimilarity(
            String[] realPathParts, String[] searchedPathParts) {
        int counter = 0;
        String searchedPart;
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
            if ( containsIgnoreCase(realPart, searchedPart) || 
                 isSimilar(realPart, searchedPart) ) {
                counter++;
            } 
        }
        
        return ( counter == searchedPathParts.length );
    }

    private Stream<Path> prepareCollectingPathStream(Path root, FileSearchMode mode) throws IOException {        
        return Files
                .walk(root, this.searchDepth, FOLLOW_LINKS)
                .filter(path -> mode.correspondsTo(path))
                .map(path -> root.relativize(path))
                .peek(path -> System.out.println(path.toString()))
                .filter(path -> this.filterSystemFiles(path));
    }

    @Override
    public List<String> collectAll(Path root, FileSearchMode mode) throws IOException {
        return this.prepareCollectingPathStream(root, mode)
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }
    
    @Override
    public List<String> collectByStrictName(
            Path root, String nameToFind, FileSearchMode mode) 
            throws IOException {
        return this.prepareCollectingPathStream(root, mode)
                .filter(path -> this.filterByStrictName(nameToFind, path))
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }

    @Override
    public List<String> collectByNamePatternSimilarity(
            Path root, String nameToFind, FileSearchMode mode)
            throws IOException {        
        return this.prepareCollectingPathStream(root, mode)
                .filter(path -> this.filterByNamePatternSimilarity(nameToFind, asName(path)))
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }
    
    @Override
    public List<String> collectBySubpathPatternSimilarityIgnoreSeparators(
            Path root, String nameToFind, FileSearchMode mode)
            throws IOException {        
        return this.prepareCollectingPathStream(root, mode)
                .map(path -> path.toString())
                .filter(fileName -> this.filterByNamePatternSimilarity(nameToFind, fileName))
                .collect(toList());
    }
    
    @Override
    public List<String> collectByWholeNamePattern(
            Path root, String nameToFind, FileSearchMode mode)
            throws IOException {        
        return this.prepareCollectingPathStream(root, mode)
                .filter(path -> containsIgnoreCase(asName(path), nameToFind))
                .peek(path -> System.out.println("after contains(): " + path.getFileName().toString()))
                .map(path -> normalizeSeparators(path.toString()))
                .peek(path -> System.out.println("collecting: " + path))
                .collect(toList());
    }
    
    @Override
    public List<String> collectByPathPartsSimilarity(
            Path root, String target, FileSearchMode mode) 
            throws IOException {
        
        String[] targetPathParts = splitPathFragmentsFrom(target);
        
        return Files
                .walk(root, this.searchDepth, FOLLOW_LINKS)
                .filter(path -> mode.correspondsTo(path))
                .map(path -> root.relativize(path))
                .filter(path -> this.filterSystemFiles(path))
                .filter(path -> this.filterByPathPartsSimilarity(
                        splitToParts(path), targetPathParts))
                .map(path -> normalizeSeparators(path.toString()))
                .collect(toList());
    }
}
