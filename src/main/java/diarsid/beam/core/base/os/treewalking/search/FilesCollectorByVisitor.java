/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector;

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
    private final FolderTypeDetector folderTypeDetector;
    private final NameDetector nameDetectorStub;

    FilesCollectorByVisitor(int searchDepth, FolderTypeDetector folderTypeDetector) {          
        debug("[FILE SEARCH] by visitor");    
        this.searchDepth = searchDepth;
        this.folderTypeDetector = folderTypeDetector;
        this.nameDetectorStub = new NameDetector(null) {
            @Override
            boolean isMatch(Path testedPath) {
                return true;
            }
        };
    }
    
    private List<String> collectWith(Path root, FileSearchMode mode, NameDetector detector) 
            throws IOException{
        FileVisitorForCollecting visitor = new FileVisitorForCollecting(
                mode, root, detector, this.folderTypeDetector);
        walkFileTree(root, FILE_VISIT_OPTIONS, this.searchDepth, visitor);
        return visitor.collected();
    }

    @Override
    public List<String> collectAll(Path root, FileSearchMode mode) throws IOException {
        FileVisitorForCollecting visitor = new FileVisitorForCollecting(
                mode, root, this.nameDetectorStub, this.folderTypeDetector);
        walkFileTree(root, FILE_VISIT_OPTIONS, this.searchDepth, visitor);
        return visitor.collected();
    }

    @Override
    public List<String> collectByNamePatternSimilarity(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        NameDetector detector = new DetectorForNameSimilarity(nameToFind);
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectByPathPartsSimilarity(
            Path root, String pathToFind, FileSearchMode mode) throws IOException {
        NameDetector detector = new DetectorForPathPartsSimilarity(splitPathFragmentsFrom(pathToFind));
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectByStrictName(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        NameDetector detector = new DetectorForStrictNameMatch(nameToFind);
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectBySubpathPatternSimilarityIgnoreSeparators(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        NameDetector detector = new DetectorForSubpathSimilarity(nameToFind);
        return this.collectWith(root, mode, detector);
    }

    @Override
    public List<String> collectByWholeNamePattern(
            Path root, String nameToFind, FileSearchMode mode) throws IOException {
        NameDetector detector = new DetectorForWholePatternMatch(nameToFind);
        return this.collectWith(root, mode, detector);
    }
    
}
