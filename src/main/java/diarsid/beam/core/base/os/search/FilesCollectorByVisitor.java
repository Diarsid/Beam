/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.walkFileTree;
import static java.util.Collections.emptySet;

import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.PathUtils.splitPathFragmentsFrom;

/**
 *
 * @author Diarsid
 */
class FilesCollectorByVisitor implements FilesCollector {    
    
    private static final Set<FileVisitOption> FILE_VISIT_OPTIONS;
    
    static {
        FILE_VISIT_OPTIONS = emptySet();
    }

    private final int searchDepth;

    FilesCollectorByVisitor(int searchDepth) {          
        debug("[FILE SEARCH] by visitor");    
        this.searchDepth = searchDepth;
    }
    
    private List<String> collectWith(Path root, FileSearchMode mode, Detector detector) 
            throws IOException{
        FileVisitorForCollecting visitor = new FileVisitorForCollecting(mode, root, detector);
        walkFileTree(root, FILE_VISIT_OPTIONS, this.searchDepth, visitor);
        return visitor.collected();
    }

    @Override
    public List<String> collectByNamePatternSimilarity(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        Detector detector = new DetectorForNameSimilarity(nameToFind);
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectByPathPartsSimilarity(
            Path root, String pathToFind, FileSearchMode mode) throws IOException {
        Detector detector = new DetectorForPathPartsSimilarity(splitPathFragmentsFrom(pathToFind));
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectByStrictName(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        Detector detector = new DetectorForStrictNameMatch(nameToFind);
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectBySubpathPatternSimilarityIgnoreSeparators(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        Detector detector = new DetectorForSubpathSimilarity(nameToFind);
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectByWholeNamePattern(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        Detector detector = new DetectorForWholePatternMatch(nameToFind);
        return this.collectWith(root, mode, detector);
    }
    
}
