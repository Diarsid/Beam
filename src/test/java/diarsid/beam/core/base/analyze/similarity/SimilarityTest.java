/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.similarity;


import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.util.CollectionsUtils.toSet;

/**
 *
 * @author Diarsid
 */
public class SimilarityTest {
    
    public Similarity similarity = new Similarity(configuration());
    
    @Test
    public void test_isSimilar() {
        assertFalse(similarity.isSimilar("AAaaDir", "foldile"));
    }
    
    @Test
    public void test_isSimilar_get_gte() {
        assertTrue(similarity.isSimilar("get", "gte"));
    }
    
    @Test
    public void test_isSimilar_get_tge() {
        assertFalse(similarity.isSimilar("get", "tge"));
    }
    
    @Test
    public void test_isSimilar_get_tag() {
        assertFalse(similarity.isSimilar("get", "tag"));
    }
    
    @Test
    public void test_isSimilar_apacheds_messea() {
        assertFalse(similarity.isSimilar("apacheds", "messea"));
    }
    
    @Test
    public void test_isSimilar_get_teg() {
        assertFalse(similarity.isSimilar("get", "teg"));
    }
    
    @Test
    public void test_isSimilar_page_paeg() {
        assertTrue(similarity.isSimilar("page", "paeg"));
    }
    
    @Test
    public void test_isSimilar_inner_ier() {
        assertTrue(similarity.isSimilar("inner", "ier"));
    }
    
    @Test
    public void test_isSimilar_page_apeg() {
        assertFalse(similarity.isSimilar("page", "apeg"));
    }
    
    @Test
    public void test_isSimilar_webpage_wepaeg() {
        assertTrue(similarity.isSimilar("webpage", "wepaeg"));
    }
    
    @Test
    public void test_isSimilar_open_opn() {
        assertTrue(similarity.isSimilar("open", "opn"));
    }
    
    @Test
    public void test_isSimilar_sigmar_sigram() {
        assertTrue(similarity.isSimilar("sigmar", "sigram"));
    }
    
    @Test
    public void test_isSimilar_SoulWindows7_win7() {
        assertTrue(similarity.isSimilar("SoulWindows7upd12fw446fngg67jf.exe", "win7"));
    }
    
    @Test
    public void test_isSimilar_exit_ext() {
        assertTrue(similarity.isSimilar("exit", "ext"));
    }
    
    @Test
    public void test_isSimilar_exit_exet() {
        assertTrue(similarity.isSimilar("exit", "exet"));
    }
    
    @Test
    public void test_isSimilar_game_games() {
        assertTrue(similarity.isSimilar("game", "games"));
    }
    
    @Test
    public void test_isSimilar_exit_exut() {
        assertTrue(similarity.isSimilar("exit", "exut"));
    }
    
    @Test
    public void test_isSimilar_locations_locastion() {
        assertTrue(similarity.isSimilar("locations", "locastion"));
    }
    
    @Test
    public void test_isSimilar_webpage_egpawe_false() {
        assertFalse(similarity.isSimilar("webpage", "egpawe"));
    }
    
    @Test
    public void test_isSimilar_projects_programs_false() {
        assertFalse(similarity.isSimilar("projects", "programs"));
    }
    
    @Test
    public void test_isSimilar_projects_phots_false() {
        assertFalse(similarity.isSimilar("projects", "phots"));
    }
    
    @Test
    public void test_isSimilar_projects_photos_false() {
        assertFalse(similarity.isSimilar("projects", "photos"));
    }
    
    @Test
    public void test_isSimilar_images_games_false() {
        assertFalse(similarity.isSimilar("images", "games"));
    }
    
    @Test
    public void test_isSimilar_programs_proagm() {
        assertTrue(similarity.isSimilar("programs", "proagm"));
    }
    
    @Test
    public void test_isSimilar_jshell_shall() {
        assertTrue(similarity.isSimilar("jshell", "shall"));
    }
    
    @Test
    public void test_isSimilar_folderinner_inr() {
        assertTrue(similarity.isSimilar("folder_1/inner/bbbb.txt", "inr"));
    }
    
    @Test
    public void test_isSimilar_folder_1_foldile() {
        assertTrue(similarity.isSimilar("folder_1", "foldile"));
    }
    
    @Test
    public void test_webpanel_paeg() {
        assertFalse(similarity.isSimilar("webpanel", "paeg"));
    }
    
    @Test
    public void test_isSimilar_webpanel_webpnel() {
        assertTrue(similarity.isSimilar("webpanel", "webpnel"));
    }
    
    @Test
    public void test_isSimilar_directory_dierctry() {
        assertTrue(similarity.isSimilar("directory", "dierctry"));
    }
    
    @Test
    public void test_isSimilar_engines_eninges() {
        assertTrue(similarity.isSimilar("engines", "eninges"));
    }
    
    @Test
    public void test_isSimilar_design_engines() {
        assertFalse(similarity.isSimilar("design", "engines"));
    }
    
    @Test
    public void test_isSimilar_webpanel_peabwlne() {
        assertFalse(similarity.isSimilar("webpanel", "peabwlne"));
    }
    
    @Test
    public void test_isSimilar_directory_yretdoicr() {
        assertFalse(similarity.isSimilar("directory", "yretdoicr"));
    }
    
    @Test
    public void test_isSimilar_panel_page() {
        assertFalse(similarity.isSimilar("panel", "page"));
    }
    
    @Test
    public void test_isSimilar_edit_delet() {
        assertFalse(similarity.isSimilar("edit", "delet"));
    }
    
    @Test
    public void test_isSimilar_delete_delet() {
        assertTrue(similarity.isSimilar("delete", "delete"));
    }
    
    @Test
    public void test_isSimilar_notes() {
        assertTrue(similarity.isSimilar("notes", "noets"));
    }
    
    @Test
    public void test_isSimilar_note() {
        assertTrue(similarity.isSimilar("notes", "noet"));
    }
    
    @Test
    public void test_isSimilar_Live_At_Donington() {
        assertTrue(similarity.isSimilar("1994 - Live_At_Donington_CD1", "livdeongi"));
    }
    
    @Test
    public void test_isSimilar_A_Matter_of_Life_and_Death() {
        assertTrue(similarity.isSimilar("2006_A_Matter_of_Life_and_Death", "lifedeath"));
    }
    
    @Test
    public void test_isSimilar_webpanel() {
        assertTrue(similarity.isSimilar("webpanel", "wepanel"));
        assertTrue(similarity.isSimilar("webpanel", "wepanl"));
    }
    
    @Test
    public void test_isSimilar_create() {
        assertTrue(similarity.isSimilar("create", "cretae"));
        assertTrue(similarity.isSimilar("create", "crate"));
    }
    
    @Test
    public void test_isSimilar_page_get() {
        assertFalse(similarity.isSimilar("page", "get"));
    }
    
    @Test
    public void test_isSimilar_page_image() {
        assertFalse(similarity.isSimilar("page", "image"));
    } 
    
    @Test
    public void test_isSimilar_dirct_directory() {
        assertTrue(similarity.isSimilar("directory", "dirct"));
    }
    
    @Test
    public void test_isSimilar_shorehowrd_2__Store_Howard_Shore() {
        assertTrue(similarity.isSimilar("2__Store/Howard Shore", "shorehowrd"));
    }
    
    @Test
    public void test_isSimilar_ambein_ambient() {
        assertTrue(similarity.isSimilar("The Hobbit (Calm Ambient Mix by Syneptic)  Episode II.mp3", "ambein"));
    }
    
    @Test
    public void test_isSimilar_amibent_ambient() {
        assertTrue(similarity.isSimilar("The Hobbit (Calm Ambient Mix by Syneptic)  Episode II.mp3", "amibent"));
    }
    
    @Test
    public void test_hasSimilarIgnoreCase_directory() {
        assertTrue(similarity.hasSimilar(toSet(
                "dir", 
                "direct", 
                "directory"), "dirct"));
    }
    
    @Test
    public void test_hasSimilarIgnoreCase_get_gte() {
        assertTrue(similarity.hasSimilar(toSet(
                "?", 
                "find", 
                "get"), "gte"));
    }
    
    @Test
    public void test_hasSimilarIgnoreCase_find() {
        assertTrue(similarity.hasSimilar(toSet(
                "?", 
                "get", 
                "find"), "fnd"));
    }
    
    @Test
    public void test_hasSimilarIgnoreCase_page() {
        assertTrue(similarity.hasSimilar(toSet(
                "page", 
                "webpage", 
                "webp", 
                "web"), "webpge"));
    }
    
    @Test
    public void test_hasSimilar_exit() {
        assertFalse(similarity.hasSimilar(toSet(
                "call", 
                "exe", 
                "exec"), "exit"));
    }
    
    @Test
    public void test_hasSimilar_loc() {
        assertTrue(similarity.hasSimilar(toSet(
                "loc", 
                "location"), "loc"));
    }
    
    @Test
    public void test_hasSimilar_loc_false() {
        assertFalse(similarity.hasSimilar(toSet(
                "mem", 
                "memory", 
                "com", 
                "comm", 
                "command"), "loc"));
    }
    
    @Test
    public void isSimilarPathToPath_hiring_Cvs_hiring_cv() {
        assertTrue(similarity.isSimilarPathToPath("Current/Hiring/CVs", "hirng/cv"));
    }
    
    @Test
    public void isSimilar_hiring_Cvs_hiringcv() {
        assertTrue(similarity.isSimilar("Current/Hiring/CVs", "hirngcv"));
    }
    
    @Test
    public void isSimilar_hiring_Cvs_hiring_cv() {
        assertTrue(similarity.isSimilar("Current/Hiring/CVs", "hirng/cv"));
    }
}
