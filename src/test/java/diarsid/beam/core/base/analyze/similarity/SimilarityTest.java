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
import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilarPathToPath;
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
    public void test_isSimilar_get_gte() {
        assertTrue(isSimilar("get", "gte"));
    }
    
    @Test
    public void test_isSimilar_get_tge() {
        assertFalse(isSimilar("get", "tge"));
    }
    
    @Test
    public void test_isSimilar_get_tag() {
        assertFalse(isSimilar("get", "tag"));
    }
    
    @Test
    public void test_isSimilar_get_teg() {
        assertFalse(isSimilar("get", "teg"));
    }
    
    @Test
    public void test_isSimilar_page_paeg() {
        assertTrue(isSimilar("page", "paeg"));
    }
    
    @Test
    public void test_isSimilar_inner_ier() {
        assertTrue(isSimilar("inner", "ier"));
    }
    
    @Test
    public void test_isSimilar_page_apeg() {
        assertFalse(isSimilar("page", "apeg"));
    }
    
    @Test
    public void test_isSimilar_webpage_wepaeg() {
        assertTrue(isSimilar("webpage", "wepaeg"));
    }
    
    @Test
    public void test_isSimilar_open_opn() {
        assertTrue(isSimilar("open", "opn"));
    }
    
    @Test
    public void test_isSimilar_SoulWindows7_win7() {
        assertTrue(isSimilar("SoulWindows7upd12fw446fngg67jf.exe", "win7"));
    }
    
    @Test
    public void test_isSimilar_exit_ext() {
        assertTrue(isSimilar("exit", "ext"));
    }
    
    @Test
    public void test_isSimilar_exit_exet() {
        assertTrue(isSimilar("exit", "exet"));
    }
    
    @Test
    public void test_isSimilar_exit_exut() {
        assertTrue(isSimilar("exit", "exut"));
    }
    
    @Test
    public void test_isSimilar_locations_locastion() {
        assertTrue(isSimilar("locations", "locastion"));
    }
    
    @Test
    public void test_isSimilar_webpage_egpawe_false() {
        assertFalse(isSimilar("webpage", "egpawe"));
    }
    
    @Test
    public void test_isSimilar_projects_programs_false() {
        assertFalse(isSimilar("projects", "programs"));
    }
    
    @Test
    public void test_isSimilar_projects_phots_false() {
        assertFalse(isSimilar("projects", "phots"));
    }
    
    @Test
    public void test_isSimilar_projects_photos_false() {
        assertFalse(isSimilar("projects", "photos"));
    }
    
    @Test
    public void test_isSimilar_images_games_false() {
        assertFalse(isSimilar("images", "games"));
    }
    
    @Test
    public void test_isSimilar_programs_proagm() {
        assertTrue(isSimilar("programs", "proagm"));
    }
    
    @Test
    public void test_isSimilar_jshell_shall() {
        assertTrue(isSimilar("jshell", "shall"));
    }
    
    @Test
    public void test_isSimilar_folderinner_inr() {
        assertTrue(isSimilar("folder_1/inner/bbbb.txt", "inr"));
    }
    
    @Test
    public void test_isSimilar_folder_1_foldile() {
        assertTrue(isSimilar("folder_1", "foldile"));
    }
    
    @Test
    public void test_webpanel_paeg() {
        assertFalse(isSimilar("webpanel", "paeg"));
    }
    
    @Test
    public void test_isSimilar_webpanel_webpnel() {
        assertTrue(isSimilar("webpanel", "webpnel"));
    }
    
    @Test
    public void test_isSimilar_directory_dierctry() {
        assertTrue(isSimilar("directory", "dierctry"));
    }
    
    @Test
    public void test_isSimilar_engines_eninges() {
        assertTrue(isSimilar("engines", "eninges"));
    }
    
    @Test
    public void test_isSimilar_design_engines() {
        assertFalse(isSimilar("design", "engines"));
    }
    
    @Test
    public void test_isSimilar_webpanel_peabwlne() {
        assertFalse(isSimilar("webpanel", "peabwlne"));
    }
    
    @Test
    public void test_isSimilar_directory_yretdoicr() {
        assertFalse(isSimilar("directory", "yretdoicr"));
    }
    
    @Test
    public void test_isSimilar_panel_page() {
        assertFalse(isSimilar("panel", "page"));
    }
    
    @Test
    public void test_isSimilar_edit_delet() {
        assertFalse(isSimilar("edit", "delet"));
    }
    
    @Test
    public void test_isSimilar_delete_delet() {
        assertTrue(isSimilar("delete", "delete"));
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
    public void test_isSimilar_dirct_directory() {
        assertTrue(isSimilar("directory", "dirct"));
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
    public void test_hasSimilar_exit() {
        assertFalse(hasSimilar(toSet(
                "call", 
                "exe", 
                "exec"), "exit"));
    }
    
    @Test
    public void test_hasSimilar_loc() {
        assertTrue(hasSimilar(toSet(
                "loc", 
                "location"), "loc"));
    }
    
    @Test
    public void test_hasSimilar_loc_false() {
        assertFalse(hasSimilar(toSet(
                "mem", 
                "memory", 
                "com", 
                "comm", 
                "command"), "loc"));
    }
    
    @Test
    public void isSimilarPathToPath_hiring_Cvs_hiring_cv() {
        assertTrue(isSimilarPathToPath("Current/Hiring/CVs", "hirng/cv"));
    }
    
    @Test
    public void isSimilar_hiring_Cvs_hiringcv() {
        assertTrue(isSimilar("Current/Hiring/CVs", "hirngcv"));
    }
    
    @Test
    public void isSimilar_hiring_Cvs_hiring_cv() {
        assertTrue(isSimilar("Current/Hiring/CVs", "hirng/cv"));
    }
}
