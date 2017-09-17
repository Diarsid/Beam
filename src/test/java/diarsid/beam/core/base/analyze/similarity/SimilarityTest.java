/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;


import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static diarsid.beam.core.base.analyze.similarity.Similarity.hasSimilar;
import static diarsid.beam.core.base.analyze.similarity.Similarity.hasStrictSimilar;
import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.analyze.similarity.Similarity.isStrictSimilar;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;

/**
 *
 * @author Diarsid
 */
public class SimilarityTest {
    
    @Test
    public void test_isSimilar() {
        assertFalse(isSimilar("AAaaDir", "foldile"));
    }
    
    @Test
    public void test_isSimilar_get() {
        assertTrue(isSimilar("get", "gte"));
    }
    
    @Test
    public void test_isSimilar_page() {
        assertTrue(isSimilar("page", "paeg"));
    }
    
    @Test
    public void test_isSimilar_webpage_wepaeg() {
        assertTrue(isSimilar("webpage", "wepaeg"));
    }
    
    @Test
    public void test_isSimilarStrong_page_paeg() {
        assertTrue(isStrictSimilar("page", "paeg"));
    }
    
    @Test
    public void test_isSimilarStrong_webpanel_paeg() {
        assertTrue(isStrictSimilar("page", "paeg"));
    }
    
    @Test
    public void test_isSimilarStrong_webpanel_webpnel() {
        assertTrue(isStrictSimilar("webpanel", "webpnel"));
    }
    
    @Test
    public void test_isSimilarStrong_panel_page() {
        assertFalse(isStrictSimilar("panel", "page"));
    }
    
    @Test
    public void test_isSimilarStrong_edit_delet() {
        assertFalse(isStrictSimilar("edit", "delet"));
    }
    
    @Test
    public void test_isSimilarStrong_delete_delet() {
        assertTrue(isStrictSimilar("delete", "delete"));
    }
    
    @Test
    public void test_isSimilarStrong_get_gte() {
        assertTrue(isStrictSimilar("get", "gte"));
    }
    
    @Test
    public void test_isSimilar_notes() {
        assertTrue(isSimilar("notes", "noets"));
    }
    
    @Test
    public void test_isSimilar_note() {
        assertTrue(isSimilar("notes", "noet"));
    }
    
    @Test
    public void test_isSimilar_webpanel() {
        assertTrue(isSimilar("webpanel", "wepanel"));
        assertTrue(isSimilar("webpanel", "wepanl"));
    }
    
    @Test
    public void test_isSimilar_create() {
        assertTrue(isSimilar("create", "cretae"));
        assertTrue(isSimilar("create", "crate"));
    }
    
    @Test
    public void test_isSimilar_page_get() {
        assertFalse(isSimilar("page", "get"));
    }
    
    @Test
    public void test_isSimilar_page_image() {
        assertFalse(isSimilar("page", "image"));
    }    
    
    @Test
    public void test_hasSimilarIgnoreCase_directory() {
        assertTrue(hasSimilar(toSet(
                                    "dir", 
                                    "direct", 
                                    "directory"), "dirct"));
    }
    
    @Test
    public void test_hasSimilarIgnoreCase_get_gte() {
        assertTrue(hasSimilar(toSet(
                                    "?", 
                                    "find", 
                                    "get"), "gte"));
    }
    
    @Test
    public void test_hasSimilarIgnoreCase_find() {
        assertTrue(hasSimilar(toSet(
                                    "?", 
                                    "get", 
                                    "find"), "fnd"));
    }
    
    @Test
    public void test_hasSimilarIgnoreCase_page() {
        assertTrue(hasSimilar(toSet(
                                    "page", 
                                    "webpage", 
                                    "webp", 
                                    "web"), "webpge"));
    }
    
    @Test
    public void test_hasStrictSimilar_exit() {
        assertFalse(hasStrictSimilar(toSet(
                                            "call", 
                                            "exe", 
                                            "exec"), "exit"));
    }
}
