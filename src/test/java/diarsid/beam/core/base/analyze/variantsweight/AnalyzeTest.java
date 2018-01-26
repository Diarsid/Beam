/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static org.junit.Assert.fail;

import static diarsid.beam.core.base.analyze.variantsweight.Analyze.weightVariants;
import static diarsid.beam.core.base.control.io.base.interaction.Variants.stringsToVariants;

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
    
    @Before
    public void setUp() {
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
                "beam_server_project");
        
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
    public void test_toolsCase_tols() {
        pattern = "tols";
        
        variants = asList(
                "LostFilm", 
                "Dev/3__Tools");
        
        expected = asList( 
                "Dev/3__Tools");
        
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
                "Projects/UkrPoshta/UkrPostAPI");
        
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
    public void test_projectsUkrPoshta_pstoapi() {
        pattern = "pstoapi";
        
        variants = asList(            
                "Projects/UkrPoshta",
                "Projects/UkrPoshta/CainiaoAPI",
                "Projects/UkrPoshta/UkrPostAPI");
        
        expected = asList(
                "Projects/UkrPoshta/UkrPostAPI");
        
        weightVariantsAndCheckMatching();
    }
    
    private void weightVariantsAndCheckMatching() {
        weightedVariants = weightVariants(pattern, stringsToVariants(variants));
        
        String expectedVariant;
        String actualVariant;
        List<WeightedVariant> nextSimilarVariants;
        
        List<String> reports = new ArrayList();        
        reports.add("\n === Diff with expected === ");
        
        AtomicInteger counter = new AtomicInteger(0);
        int mismatches = 0;
        
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
                    
                    expectedVariant = expected.get(counter.getAndIncrement());
                    actualVariant = weightedVariant.text();
                    
                    if ( actualVariant.equalsIgnoreCase(expectedVariant) ) {
                        reports.add(format("\n%s variant matches expected: %s", counter.get() - 1, expectedVariant));
                    } else {
                        mismatches++;
                        reports.add(format(
                            "\n%s variant does not match expected: \n" +
                            "    expected : %s\n" +
                            "    actual   : %s", counter.get() - 1, expectedVariant, actualVariant));
                    }
                }
            }           
        } 
        
        boolean hasMissed = counter.get() < expected.size();
        List<String> expectedButMissed = new ArrayList<>();
        if ( hasMissed ) {            
            expectedButMissed.add("\n === Expected but missed === ");
            
            while ( counter.get() < expected.size() ) {                
                expectedButMissed.add(format("\n%s variant missed: %s", counter.get(), expected.get(counter.getAndIncrement())));
            }
        }
            
        if ( mismatches > 0 || hasMissed ) {    
            reports.addAll(expectedButMissed);
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
