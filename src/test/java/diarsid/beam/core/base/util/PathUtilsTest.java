/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.support.objects.Pair;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import static diarsid.beam.core.base.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.base.util.PathUtils.decomposePath;
import static diarsid.beam.core.base.util.PathUtils.findDepthOf;
import static diarsid.beam.core.base.util.PathUtils.indexOfNextPathSeparatorAfter;
import static diarsid.beam.core.base.util.PathUtils.subpathToPattern;
import static diarsid.beam.core.base.util.PathUtils.toSubpathAndTarget;

/**
 *
 * @author Diarsid
 */
public class PathUtilsTest {

    public PathUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of pathIsDirectory method, of class PathUtils.
     */
    @Test
    public void testPathIsDirectory_String() {
    }

    /**
     * Test of pathIsDirectory method, of class PathUtils.
     */
    @Test
    public void testPathIsDirectory_Path() {
    }

    /**
     * Test of indexOfFirstPathSeparator method, of class PathUtils.
     */
    @Test
    public void testIndexOfFirstPathSeparator() {
    }

    /**
     * Test of joinPathFrom method, of class PathUtils.
     */
    @Test
    public void testCombinePathFrom() {
    }

    /**
     * Test of indexOfNextPathSeparatorAfter method, of class PathUtils.
     */
    @Test
    public void testIndexOfNextPathSeparatorAfter() {
        String path = "some/long\\path/to_certain\\target";
        
        String pattern = "cert";
        assertEquals(
                "some/long\\path/to_certain\\".length() - 1, 
                indexOfNextPathSeparatorAfter(path, pattern));
    }

    /**
     * Test of indexOfLastPathSeparator method, of class PathUtils.
     */
    @Test
    public void testIndexOfLastPathSeparator() {
    }

    /**
     * Test of containsPathSeparator method, of class PathUtils.
     */
    @Test
    public void testContainsPathSeparator() {
        String path1 = "some/path";
        assertTrue(containsPathSeparator(path1));
        
        String path2 = "some\\path";
        assertTrue(containsPathSeparator(path2));
        
        String path3 = "no_path";
        assertFalse(containsPathSeparator(path3));
    }

    /**
     * Test of isAcceptableWebPath method, of class PathUtils.
     */
    @Test
    public void testIsAcceptableWebPath() {
    }    
    
    @Test
    public void testSubpathToPattern() {
        String fullPath = "some/long/full/path\\to_the/another\\folder/with\\some/documents";
        
        String pattern1 = "ome/fold";
        assertEquals(
                "some/long/full/path\\to_the/another\\folder", 
                subpathToPattern(fullPath, pattern1));
        
        String pattern2 = "ome\\fold";
        assertEquals(
                "some/long/full/path\\to_the/another\\folder", 
                subpathToPattern(fullPath, pattern2));
        
        String pattern3 = "/old";
        assertEquals(
                "some/long/full/path\\to_the/another\\folder", 
                subpathToPattern(fullPath, pattern3));
        
        String pattern4 = "fold";
        assertEquals(
                "some/long/full/path\\to_the/another\\folder", 
                subpathToPattern(fullPath, pattern4));
    }    

    /**
     * Test of isAcceptableFilePath method, of class PathUtils.
     */
    @Test
    public void testIsAcceptableFilePath() {
    }

    /**
     * Test of isAcceptableRelativePath method, of class PathUtils.
     */
    @Test
    public void testIsAcceptableRelativePath() {
    }

    /**
     * Test of extractLocationFromPath method, of class PathUtils.
     */
    @Test
    public void testExtractLocationFromPath() {
    }

    /**
     * Test of extractTargetFromPath method, of class PathUtils.
     */
    @Test
    public void testExtractTargetFromPath() {
    }

    /**
     * Test of trimSeparators method, of class PathUtils.
     */
    @Test
    public void testTrimSeparatorsInBothEnds() {
    }

    /**
     * Test of normalizeArgument method, of class PathUtils.
     */
    @Test
    public void testNormalizeArgument() {
    }

    /**
     * Test of normalizeSeparators method, of class PathUtils.
     */
    @Test
    public void testNormalizeSeparators() {
    }

    /**
     * Test of splitPathFragmentsFrom method, of class PathUtils.
     */
    @Test
    public void testNormalizePathFragmentsFrom() {
    }

    /**
     * Test of asRelativeString method, of class PathUtils.
     */
    @Test
    public void testRelativizeFileName() {
    }
    
    @Test
    public void testDecomposePath() {
        String path = "some/path/to/decompose";
        List<String> expectedDecomposedPaths = asList(
                "some/path/to/decompose", 
                "some/path/to", 
                "some/path", 
                "some");
        assertEquals(expectedDecomposedPaths, decomposePath(path));
    }

    @Test
    public void testToSubpathAndTarget() {
        String path = "some/path/to/target";
        Pair<String, String> subpathTarget = toSubpathAndTarget(path);
        assertThat(subpathTarget.first(), equalTo("some/path/to"));
        assertThat(subpathTarget.second(), equalTo("target"));
    }
    
    @Test
    public void testFindDepthOf_notPath() {
        String path = "not a path";
        assertThat(findDepthOf(path), equalTo(1));
    }
    
    @Test
    public void testFindDepthOf_path_usual() {
        String path = "is a/path";
        assertThat(findDepthOf(path), equalTo(2));
    }
    
    @Test
    public void testFindDepthOf_pathDepth2_duplicatedSeparators() {
        String path;
        
        path = "is a//path";
        assertThat(findDepthOf(path), equalTo(2));
        path = "is a///path";
        assertThat(findDepthOf(path), equalTo(2));
        path = "is a///\\path";
        assertThat(findDepthOf(path), equalTo(2));
    }
    
    @Test
    public void testFindDepthOf_pathDepth3_duplicatedSeparators() {
        String path;
        
        path = "this\\is a//path";
        assertThat(findDepthOf(path), equalTo(3));
        path = "this/is a/path";
        assertThat(findDepthOf(path), equalTo(3));
        path = "this////is a\\path";
        assertThat(findDepthOf(path), equalTo(3));
    }
    
    @Test
    public void testFindDepthOf_pathDepth3_leadingSeparators() {
        String path;
        
        path = "/this/is a/path";
        assertThat(findDepthOf(path), equalTo(3));
        path = "///this/is a/path";
        assertThat(findDepthOf(path), equalTo(3));
        path = "/\\/this/is a/path";
        assertThat(findDepthOf(path), equalTo(3));
    }
    
    @Test
    public void testFindDepthOf_notPath_leadingSeparators() {
        String path;
        
        path = "/not a path";
        assertThat(findDepthOf(path), equalTo(1));
        path = "///not a path";
        assertThat(findDepthOf(path), equalTo(1));
        path = "/\\/not a path";
        assertThat(findDepthOf(path), equalTo(1));
    }
    
    @Test
    public void testFindDepthOf_pathDepth3_trailingSeparators() {
        String path;
        
        path = "this/is a/path/";
        assertThat(findDepthOf(path), equalTo(3));
        path = "this/is a/path///";
        assertThat(findDepthOf(path), equalTo(3));
        path = "this/is a/path/\\/";
        assertThat(findDepthOf(path), equalTo(3));
    }
    
    @Test
    public void testFindDepthOf_notPath_trailingSeparators() {
        String path;
        
        path = "not a path/";
        assertThat(findDepthOf(path), equalTo(1));
        path = "not a path///";
        assertThat(findDepthOf(path), equalTo(1));
        path = "not a path/\\/";
        assertThat(findDepthOf(path), equalTo(1));
    }
    
    @Test
    public void testFindDepthOf_pathDepth3_leadingAndTrailingSeparators() {
        String path;
        
        path = "/this/is a/path/";
        assertThat(findDepthOf(path), equalTo(3));
        path = "///this/is a/path///";
        assertThat(findDepthOf(path), equalTo(3));
        path = "/\\/this/is a/path/\\/";
        assertThat(findDepthOf(path), equalTo(3));
    }
    
    @Test
    public void testFindDepthOf_notPath_leadingAndTrailingSeparators() {
        String path;
        
        path = "/not a path/";
        assertThat(findDepthOf(path), equalTo(1));
        path = "///not a path///";
        assertThat(findDepthOf(path), equalTo(1));
        path = "/\\/not a path/\\/";
        assertThat(findDepthOf(path), equalTo(1));
    }
}