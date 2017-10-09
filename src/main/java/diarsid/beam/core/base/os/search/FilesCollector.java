/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Diarsid
 */
interface FilesCollector {

    List<String> collectByNamePatternSimilarity(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;

    List<String> collectByPathPartsSimilarity(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;

    List<String> collectByStrictName(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;

    List<String> collectBySubpathPatternSimilarityIgnoreSeparators(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;

    List<String> collectByWholeNamePattern(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;    
}
