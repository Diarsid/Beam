/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;


import org.junit.Ignore;
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
    
    // TODO MIDDLE
    @Ignore
    @Test
    public void test_isStrictSimilar_webpage_wepaeg() {
        assertTrue(isStrictSimilar("webpage", "wepaeg"));
    }
    
    @Test
    public void test_isStrictSimilar_webpage_wepaeg_false() {
        assertFalse(isStrictSimilar("webpage", "egpawe"));
    }
    
    @Test
    public void test_isStrictSimilar_page_paeg() {
        assertTrue(isStrictSimilar("page", "paeg"));
    }
    
    @Test
    public void testr_webpanel_paeg() {
        assertFalse(isStrictSimilar("webpanel", "paeg"));
    }
    
    @Test
    public void test_isStrictSimilar_webpanel_webpnel() {
        assertTrue(isStrictSimilar("webpanel", "webpnel"));
    }
    
    @Test
    public void test_isStrictSimilar_directory_dierctry() {
        assertTrue(isStrictSimilar("directory", "dierctry"));
    }
    
    @Test
    public void test_isStrictSimilar_engines_eninges() {
        assertTrue(isStrictSimilar("engines", "eninges"));
    }
    
    @Test
    public void test_isStrictSimilar_design_engines() {
        assertFalse(isStrictSimilar("design", "engines"));
    }
    
    @Test
    public void test_isStrictSimilar_webpanel_peabwlne() {
        assertFalse(isStrictSimilar("webpanel", "peabwlne"));
    }
    
    @Test
    public void test_isStrictSimilar_directory_yretdoicr() {
        assertFalse(isStrictSimilar("directory", "yretdoicr"));
    }
    
    @Test
    public void test_isStrictSimilar_panel_page() {
        assertFalse(isStrictSimilar("panel", "page"));
    }
    
    @Test
    public void test_isStrictSimilar_edit_delet() {
        assertFalse(isStrictSimilar("edit", "delet"));
    }
    
    @Test
    public void test_isStrictSimilar_delete_delet() {
        assertTrue(isStrictSimilar("delete", "delete"));
    }
    
    @Test
    public void test_isStrictSimilar_get_gte() {
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
    public void test_isStrictSimilar_page_get() {
        assertFalse(isStrictSimilar("page", "get"));
    }
    
    @Test
    public void test_isStrictSimilar_page_image() {
        assertFalse(isStrictSimilar("page", "image"));
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
