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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.base.objects.Cache;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static org.junit.Assert.fail;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;
import static diarsid.beam.core.base.objects.Cache.cacheOf;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;

/**
 *
 * @author Diarsid
 */
public class AnalyzeTest {
    
    private String pattern;
    private List<String> variants;
    private List<String> expected;
    private WeightedVariants weightedVariants;
    
    public AnalyzeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
        Optional<Cache<AnalyzeData>> cache = cacheOf(AnalyzeData.class);
        if ( cache.isPresent() ) {
            Cache<AnalyzeData> c = cache.get();
            AnalyzeData analyzeData = c.give();
        }
    }
    
    @Before
    public void setUp() {
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
                "1__Projects/Diarsid/NetBeans");
        
        expected = asList( 
                "1__Projects/Diarsid/NetBeans",
                "2__LIB/Maven_Local_Repo/io/springfox/springfox-bean-validators");
        
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
                "netbeans_projects");
        
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
    public void test_projectsUkrPoshta_ukrpsoapi() {
        pattern = "ukrpsoapi";
        
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
                "Projects/UkrPoshta/PriceCalculationAPI");
        
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
    
    private void weightVariantsAndCheckMatching() {
        weightedVariants = weightVariants(pattern, stringsToVariants(variants));
        
        String expectedVariant;
        String actualVariant;
        List<WeightedVariant> nextSimilarVariants;
        
        List<String> reports = new ArrayList();        
        List<String> presentButNotExpected = new ArrayList<>();
        reports.add("\n === Diff with expected === ");
        
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
        variantsWithWeight.add("\n === Analyze result === ");
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
        variantsWithWeight.add("");
        
        return variantsWithWeight.stream().collect(joining());
    }
    
}
