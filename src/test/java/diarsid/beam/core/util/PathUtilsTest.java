/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import static diarsid.beam.core.util.PathUtils.containsPathSeparator;
import static diarsid.beam.core.util.PathUtils.indexOfNextPathSeparatorAfter;
import static diarsid.beam.core.util.PathUtils.subpathToPattern;

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
     * Test of combinePathFrom method, of class PathUtils.
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
     * Test of trimSeparatorsInBothEnds method, of class PathUtils.
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
     * Test of normalizePathFragmentsFrom method, of class PathUtils.
     */
    @Test
    public void testNormalizePathFragmentsFrom() {
    }

    /**
     * Test of relativizeFileName method, of class PathUtils.
     */
    @Test
    public void testRelativizeFileName() {
    }

}