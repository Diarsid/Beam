/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import diarsid.beam.core.base.objects.Pool;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static org.junit.Assert.fail;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.disableResultsLimit;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.resultsLimitToDefault;
import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.objects.Pools.poolOf;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
public class AnalyzeTest {
    
    private static int totalVariantsQuantity;
    private static long start;
    private static long stop;
    
    private boolean expectedToFail;
    private String pattern;
    private List<String> variants;
    private List<String> expected;
    private WeightedVariants weightedVariants;
    
    public AnalyzeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        start = currentTimeMillis();
    }
    
    @AfterClass
    public static void tearDownClass() {
        stop = currentTimeMillis();
        Logger logger = LoggerFactory.getLogger(AnalyzeTest.class);
        String report = 
                "\n ======================================" +
                "\n ====== Total AnalyzeTest results =====" +
                "\n ======================================" +
                "\n  total time     : %s " + 
                "\n  total variants : %s \n";
        logger.info(format(report, stop - start, totalVariantsQuantity));
        Optional<Pool<AnalyzeData>> pool = poolOf(AnalyzeData.class);
        if ( pool.isPresent() ) {
            Pool<AnalyzeData> c = pool.get();
            AnalyzeData analyzeData = c.give();
        }
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        resultsLimitToDefault();
    }
    
    private void expectedToFail() {
        this.expectedToFail = true;
    }
    
    @Test
    public void test_EnginesCase_engns() {
        pattern = "engns";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EnginesCase_enges() {
        pattern = "enges";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EnginesCase_enins() {
        pattern = "enins";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_PhotosCase_phots() {
        pattern = "phots";
        
        variants = asList(
                "Projects",
                "Images/Photos",
                "Photos");
        
        expected = asList( 
                "Photos",
                "Images/Photos");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EnginesCase_enings() {
        pattern = "enings";
        
        variants = asList(
                "Engines",
                "Design");
        
        expected = asList( 
                "Engines");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_JavaTechCase_jtech() {
        pattern = "jtech";
        
        variants = asList(
                "Books/tech",
                "Tech",
                "Books/Tech/Design",
                "Tech/langs",
                "Books/Tech/Java");
        
        expected = asList( 
                "Books/Tech/Java",
                "Tech",
                "Tech/langs",
                "Books/tech",
                "Books/Tech/Design");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_JavaTechCase_jtec() {
        pattern = "jtec";
        
        variants = asList(
                "Books/tech",
                "Tech",
                "Books/Tech/Design",
                "Tech/langs",
                "Books/Tech/Java");
        
        expected = asList( 
                "Books/Tech/Java",
                "Tech",
                "Tech/langs",
                "Books/tech",
                "Books/Tech/Design");
        
        weightVariantsAndCheckMatching();
    }
    
//    @Ignore
    @Test
    public void test_techAnotherCase_jtec() {
        pattern = "jtec";
        
        variants = asList(
                "Tech",
                "Tech/langs");
        
        expected = asList(
                "Tech",
                "Tech/langs");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsBeamCase_beaporg() {
        pattern = "beaporg";
        
        variants = asList(
                "Job/Search/for_sending",
                "Projects/Diarsid/NetBeans/Beam");
        
        expected = asList(
                "Projects/Diarsid/NetBeans/Beam");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_searchSendingBeamCase_beaporg() {
        pattern = "beaporg";
        
        variants = asList(
                "Job/Search/for_sending",
                "Job/Search/Friends");
        
        expected = asList();
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_facebookCase_fb() {
        pattern = "fb";
        
        variants = asList(
                "some_Fstring_Bwith_fb_cluster",
                "facebook");
        
        expected = asList(
                "some_Fstring_Bwith_fb_cluster",
                "facebook"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_gmailWithOtherMLClusterCase_ml() {
        pattern = "ml";
        
        variants = asList(
                "some_Mstring_Lwith_ml_cluster",
                "gmail");
        
        expected = asList(
                "some_Mstring_Lwith_ml_cluster",
                "gmail"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_gmailWithOtherMLstringCase_ml() {
        pattern = "ml";
        
        variants = asList(
                "some_stMring_wLith_cluster",
                "gmail");
        
        expected = asList(
                "gmail",                
                "some_stMring_wLith_cluster"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_photosCase_phots() {
        pattern = "Phots";
        
        variants = asList(
                "Projects",
                "Photos");
        
        expected = asList(
                "Photos");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_searchSendingBeamCase_seaersengid() {
        pattern = "seaersengid";
        
        variants = asList(
                "Job/Search/for_sending",
                "Job/Search/Friends");
        
        expected = asList(
                "Job/Search/for_sending");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_ambientCase_aimbent() {
        pattern = "aimbent";
        
        variants = asList(
                "The_Hobbit_Calm_Ambient_Mix_by_Syneptic_Episode_II.mp3"
        );
        
        expected = asList(
                "The_Hobbit_Calm_Ambient_Mix_by_Syneptic_Episode_II.mp3");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_9thAgeCase_image() {
        pattern = "9age";
        
        variants = asList(
                "Image", 
                "Content/WH/Game/The_9th_Age"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age",
                "Image"
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_9thAgeRostersCase_image() {
        pattern = "9agerost";
        
        variants = asList(
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_rostersCase_rester() {
        pattern = "rester";
        
        variants = asList(
                "Dev/Start_MySQL_server",
                "Music/2__Store/Therion",
                "Content/WH/Game/The_9th_Age/Rosters"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age/Rosters",
                "Dev/Start_MySQL_server"
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_rostersCase2_rester() {
        pattern = "rester";
        
        variants = asList(
                "Dev/Start_MySQL_server",
                "Content/WH/Game/The_9th_Age/Rosters/Elves.txt"
        );
        
        expected = asList(
                "Content/WH/Game/The_9th_Age/Rosters/Elves.txt",
                "Dev/Start_MySQL_server"         
        );
        
        weightVariantsAndCheckMatching();
    }
        
    @Test
    public void test_sqlDeveloperCase_sldev() {
        pattern = "sldev";
        
        variants = asList(
                "Dev/Start_Tomcat",
                "Dev/Sql_Developer"
        );
        
        expected = asList(
                "Dev/Sql_Developer",
                "Dev/Start_Tomcat"         
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_rostersCase_rosers() {
        pattern = "rosers";
        
        variants = asList(
                "Rosters", 
                "Projects/Diarsid"
        );
        
        expected = asList(
                "Rosters"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_hboitbok() {
        pattern = "hboitbok";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_bokhboit() {
        pattern = "bokhboit";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_hobitbok() {
        pattern = "hobitbok";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hobbitBookCase_hobot() {
        pattern = "hobot";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2",
                "Images/Photos"
        );
        
        expected = asList(
                "Books/Common/Tolkien_J.R.R/The_Hobbit.fb2",
                "Images/Photos");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaHomeCase_homjav() {
        pattern = "homjav";
        
        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe",
                "Engines/Java/Path/JAVA_HOME");
        
        expected = asList(
                "Engines/Java/Path/JAVA_HOME", 
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaHomeCase_javhom() {
        pattern = "javhom";
        
        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe",
                "Engines/Java/Path/JAVA_HOME");
        
        expected = asList(
                "Engines/Java/Path/JAVA_HOME", 
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin/java.spring.exe");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_JavaTechCase_jatech() {
        pattern = "jatech";
        
        variants = asList(
                "Books/tech",
                "Tech",
                "Books/Tech/Design",
                "Tech/langs",
                "Books/Tech/Java");
        
        expected = asList( 
                "Books/Tech/Java",
                "Tech/langs");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_NetBeansCase_nebean() {
        pattern = "nebean";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam",                
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Research.Java",
                "Dev/NetBeans_8.2.lnk");
        
        expected = asList( 
                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Beam",
                "Projects/Diarsid/NetBeans/Research.Java");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_NetBeansCase_nebena() {
        pattern = "nebena";
        
        variants = asList(
                "2__LIB/Maven_Local_Repo/io/springfox/springfox-bean-validators",
                "1__Projects/Diarsid/NetBeans"
        );
        
        expected = asList( 
                "1__Projects/Diarsid/NetBeans",
                "2__LIB/Maven_Local_Repo/io/springfox/springfox-bean-validators"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_SoulProgramsCase_soulprogs() {
        pattern = "soulprogs";
        
        variants = asList(
                "Soul/programs/src",
                "Soul/programs");
        
        expected = asList( 
                "Soul/programs",
                "Soul/programs/src");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_tolkienCase_tolknei() {
        pattern = "tolknei";
        
        variants = asList(
                "tolkien");
        
        expected = asList( 
                "tolkien");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_EarthMoviesCase_soulprogs() {
        pattern = "earhmives";
        
        variants = asList(
                "Films/Movies/Middle_Earth/The_Hobbit",
                "Films/Movies/Middle_Earth");
        
        expected = asList( 
                "Films/Movies/Middle_Earth",
                "Films/Movies/Middle_Earth/The_Hobbit");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_DiarsidProjectsCase_diarsidprojecs() {
        pattern = "diarsidprojecs";
        
        variants = asList(
                "Projects/Diarsid",                
                "Projects/Diarsid/NetBeans");
        
        expected = asList(
                "Projects/Diarsid",
                "Projects/Diarsid/NetBeans");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_NetBeansCase_nebaen() {
        pattern = "nebaen";
        
        variants = asList(
                "Projects/Diarsid/NetBeans/Beam",                
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Research.Java",
                "Dev/NetBeans_8.2.lnk");
        
        expected = asList( 
                "Dev/NetBeans_8.2.lnk",
                "Projects/Diarsid/NetBeans",
                "Projects/Diarsid/NetBeans/Beam",
                "Projects/Diarsid/NetBeans/Research.Java");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_beamProjectCase_beaporj() {
        pattern = "beaporj";
        
        variants = asList(
                "beam_project_home",
                "beam_project",
                "beam_home",
                "awesome java libs",
                "git>beam",
                "beam_project/src",
                "beam netpro",
                "abe_netpro",
                "babel_pro",
                "netbeans_projects", 
                "beam_server_project"
        );
        
        expected = asList( 
                "beam_project",
                "beam_project/src",
                "beam_project_home",
                "beam_server_project",
                "netbeans_projects",
                "beam netpro",
                "abe_netpro",
                "babel_pro");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_beamProjectCase_short_beaporj() {
        pattern = "beaporj";
        
        variants = asList(
                "beam_project_home",
                "beam_server_project");
        
        expected = asList(
                "beam_project_home",
                "beam_server_project");
        
        weightVariantsAndCheckMatching();
    }

    @Test
    public void test_differens_tols() {
        
        pattern = "tols";
        
        variants = asList(
                "Tools",
                "Images/Photos",
                "Music/2__Store",
                "Projects",
                "Torrents",
                "Books/Common/Tolkien_J.R.R"
        );
        
        expected = asList(
                "Tools",
                "Books/Common/Tolkien_J.R.R",
                "Images/Photos",
                "Music/2__Store");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_enginesJavaBinCase_engjbin() {
        pattern = "engjbin";
        
        variants = asList(
                "Engines/Java/Path/JAVA_HOME/bin/java.exe",
                "Engines/Java/Path/JAVA_HOME/bin"
        );
        
        expected = asList(
                "Engines/Java/Path/JAVA_HOME/bin",
                "Engines/Java/Path/JAVA_HOME/bin/java.exe");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_jobCurrentCase_jbo_cruent() {
        pattern = "jbo/cruent";
        
        variants = asList(
                "Job/Current",
                "Current_Job/Hiring/CVs"
        );
        
        expected = asList(
                "Job/Current",
                "Current_Job/Hiring/CVs");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_toolsCase_tols() {
        pattern = "tols";
        
        variants = asList(
                "LostFilm", 
                "Dev/3__Tools"
        );
        
        expected = asList( 
                "Dev/3__Tools"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_TolkienCase_tol() {
        pattern = "tol";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R", 
                "Dev/3__Tools");
        
        expected = asList( 
                "Books/Common/Tolkien_J.R.R",
                "Dev/3__Tools");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaPathCase_jpath() {
        pattern = "jpath";
        
        variants = asList(
                "Engines/java/path", 
                "Books/Tech/Java/JavaFX", 
                "Books/Tech/Java");
    
        expected = asList( 
                "Engines/java/path");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_javaSpecCase_jspec() {
        pattern = "jspec";
        
        variants = asList(                
                "Projects/UkrPoshta/UkrPostAPI",
                "Tech/langs/Java/Specifications");
    
        expected = asList( 
                "Tech/langs/Java/Specifications");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_commonBooksCase_comboks() {
        pattern = "comboks";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R",
                "Books/Common");
        
        expected = asList( 
                "Books/Common",
                "Books/Common/Tolkien_J.R.R");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_commonBooksCase_commbooks() {
        pattern = "commbooks";
        
        variants = asList(
                "Books/Common/Tolkien_J.R.R",
                "Books/Common");
        
        expected = asList( 
                "Books/Common",
                "Books/Common/Tolkien_J.R.R");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukrposapi() {
        pattern = "ukrposapi";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukropsapi() {
        pattern = "ukropsapi";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_ukrpso() {
        pattern = "ukrpso";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList( 
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_PriceAPICase_pricapi() {
        pattern = "pricapi";
        
        variants = asList(            
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/PriceCalculationAPI");
        
        expected = asList(
                "Projects/UkrPoshta/PriceCalculationAPI",
                "Projects/UkrPoshta/CainiaoAPI");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_projectsUkrPoshta_pstoapi() {
        pattern = "pstoapi";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI",
                "Projects/UkrPoshta/CainiaoAPI");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_hiringCase_hring() {
        expectedToFail();
        
        pattern = "hring";
        
        variants = asList(            
                "Current_Job/Hiring/CVs/Java_Junior/hr",
                "Job/Current/Hiring",
                "Current_Job/hiring");
        
        expected = asList(            
                "Job/Current/Hiring",
                "Current_Job/hiring",
                "Current_Job/Hiring/CVs/Java_Junior/hr"
        );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_JavaPathBinCase_jbin() {
        pattern = "jbin";
        
        variants = asList(            
                "Current_Job/domain",
                "Current_Job/domain/tmm",
                "Current_Job/hiring",
                "Engines/java/path/JAVA_HOME/bin");
        
        expected = asList(
                "Engines/java/path/JAVA_HOME/bin");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_1() {
        pattern = "abc123";
        
        variants = asList(            
                "xy/ABC_z123er",
                "ABC/123",
                "qwCABgfg132",
                "xcdfABdC_fg123fdf23hj12");
        
        expected = asList(
                "ABC/123",
                "xy/ABC_z123er",
                "qwCABgfg132",
                "xcdfABdC_fg123fdf23hj12");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_2() {
        pattern = "abc123";
        
        variants = asList(            
                "xy/ABC_z123er/Ab",
                "xy/ABC_123er");
        
        expected = asList(       
                "xy/ABC_123er", 
                "xy/ABC_z123er/Ab"
                );
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_3() {
        pattern = "abcXYZ";
        
        variants = asList(            
                "ababbaccaABCabbac_xyyxzyyxzXYZzx",
                "ababbaccaACBabbac_xyyxzyyxzXYZzx");
        
        expected = asList(       
                "ababbaccaABCabbac_xyyxzyyxzXYZzx",
                "ababbaccaACBabbac_xyyxzyyxzXYZzx");
        
        weightVariantsAndCheckMatching();
    }
    
    @Test
    public void test_synthetic_4() {
        disableResultsLimit();
        
        pattern = "abcXYZ";
        
        variants = asList(
                "ABCXYZ",
                "ABCXYZ_acba",
                "ABCXYZ_acba/abac_xyyxz_zx",   
                "ABCXYZ_ababbacca/abbac_xyyxzyyxz_zx", 
                "zx_ABCXYZ_acba", 
                "zx_ABCXYZ_acba/abac_xyyxz_zx",
                "zx_ABCXYZ_ababbacca/abbac_xyyxzyyxz_zx", 
                "ABCXYZacba",
                "axABCXYZ_abaca/ab_xyyxz_zx",
                "axABCXYZacba", 
                "axABCXYZ_ababbacca/abbac_xyyxzyyxz_zx", 
                "axABCXYZacba_ab/ab",
                "ABC_XYZ",
                "ABC_XYZ_acb",  
                "ABC_XYZ_ababbacca/abbac_xyyxzyyxz_zx",
                "zx_ABC_XYZ", 
                "abABC_XYZ_ababbacca/abbac_xyyxzyyxz_zx",  
                "ABC_ba_XYZ_baccaba/abbac_xyyxzyyxz_zx",    
                "ABC_baccaba_XYZ_ba/abbac_xyyxzyyxz_zx",    
                "ABC_bbacbacacaba_XYZ_b/a_xyyxzyyxz_zx", 
                "ABC_ababbacca/abbac_xyyxzyyxz_XYZ_zx",  
                "ababbacca/ABC_abbac_xyyxzyyxz_XYZ_zx",     
                "ababbacca/ABC_abbac_xyyxzyyxz_XYZzx",
                "ababbacca/ABC_abbac_xyyxzyyxzXYZzx",
                "ababbacca/ABCabbac_xyyxzyyxzXYZzx",
                "ababbaccaABCabbac_xyyxzyyxzXYZzx");
        
        expected = new ArrayList<>(variants);
        
        weightVariantsAndCheckMatching();
    }
    
    private void weightVariantsAndCheckMatching() {
        boolean failed;
        try {
            totalVariantsQuantity = totalVariantsQuantity + variants.size();
            weightVariantsAndCheckMatchingInternally();
            failed = false;
        } catch (AssertionError e) {
            failed = true;
            if ( ! this.expectedToFail ) {
                throw e;
            }
        }        
        if ( ! failed && this.expectedToFail ) {
            fail("=== EXPECTED TO FAIL BUT PASSED ===");
        }
    }
    
    private void weightVariantsAndCheckMatchingInternally() {
        weightedVariants = weightVariants(pattern, stringsToVariants(variants));
        
        String expectedVariant;
        String actualVariant;
        List<WeightedVariant> nextSimilarVariants;
        
        List<String> reports = new ArrayList();        
        List<String> presentButNotExpected = new ArrayList<>();        
        
        AtomicInteger counter = new AtomicInteger(0);
        int mismatches = 0;
        
        if ( expected.isEmpty() && weightedVariants.size() > 0 ) {
            fail("No variants expected!");
        }
        
        while ( weightedVariants.next() && ( counter.get() < expected.size() ) ) {
            
            if ( weightedVariants.currentIsMuchBetterThanNext() ) {
                
                expectedVariant = expected.get(counter.getAndIncrement());
                actualVariant = weightedVariants.current().text();
                
                if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                    reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                } else {
                    mismatches++;
                    reports.add(format(
                            "\n%s variant does not match expected: \n" +
                            "    expected : %s\n" +
                            "    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                }
            } else {            
                nextSimilarVariants = weightedVariants.nextSimilarVariants();
                for (WeightedVariant weightedVariant : nextSimilarVariants) {
                    actualVariant = weightedVariant.text();
                    
                    if ( counter.get() < expected.size() ) {
                        expectedVariant = expected.get(counter.getAndIncrement());

                        if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                            reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                        } else {
                            mismatches++;
                            reports.add(format(
                                "\n%s variant does not match expected: \n" +
                                "    expected : %s\n" +
                                "    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                        }
                    } else {
                        presentButNotExpected.add(format("\n %s\n", actualVariant));
                    }    
                }
            }           
        } 
        
        if ( nonEmpty(reports) ) {
            reports.add("\n === Diff with expected === ");
        }
        
        if ( weightedVariants.size() > expected.size() ) {
            int offset = expected.size();
            String presentButNotExpectedVariant;
            for (int i = offset; i < weightedVariants.size(); i++) {
                presentButNotExpectedVariant = weightedVariants.getVariantAt(i);
                presentButNotExpected.add(format("\n %s\n", presentButNotExpectedVariant));
            }
        }
        
        boolean hasNotExpected = nonEmpty(presentButNotExpected);
        if ( hasNotExpected ) {
            presentButNotExpected.add(0, "\n === Present but not expected === ");
        }
        
        boolean hasMissed = counter.get() < expected.size();
        List<String> expectedButMissed = new ArrayList<>();
        if ( hasMissed ) {            
            expectedButMissed.add("\n === Expected but missed === ");
            
            while ( counter.get() < expected.size() ) {                
                expectedButMissed.add(format("\n%s variant missed: %s", counter.get(), expected.get(counter.getAndIncrement())));
            }
        }
            
        if ( mismatches > 0 || hasMissed || hasNotExpected ) {    
            if ( hasMissed ) {
                reports.addAll(expectedButMissed);
            }
            if ( hasNotExpected ) {
                reports.addAll(presentButNotExpected);
            }
            reports.add(0, collectVariantsToReport());
            fail(reports.stream().collect(joining()));
        }
    }
    
    private String collectVariantsToReport() {
        List<String> variantsWithWeight = new ArrayList<>();
        weightedVariants.resetTraversing();

        while ( weightedVariants.next() ) {            
            if ( weightedVariants.currentIsMuchBetterThanNext() ) {
                variantsWithWeight.add("\n" + weightedVariants.current().text() + " is much better than next: " + weightedVariants.current().weight());
            } else {
                variantsWithWeight.add("\nnext candidates are similar: ");                
                weightedVariants.nextSimilarVariants()
                        .stream()
                        .forEach(candidate -> {
                            variantsWithWeight.add("\n  - " + candidate.text() + " : " + candidate.weight());
                        });
            }
        }
        if ( nonEmpty(variantsWithWeight) ) {            
            variantsWithWeight.add(0, "\n === Analyze result === ");
        }
        variantsWithWeight.add("");
        
        return variantsWithWeight.stream().collect(joining());
    }
    
}
