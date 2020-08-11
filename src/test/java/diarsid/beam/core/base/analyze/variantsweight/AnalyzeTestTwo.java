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

import diarsid.beam.core.base.analyze.similarity.Similarity;
import diarsid.support.objects.Pool;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import static org.junit.Assert.fail;

import static diarsid.beam.core.application.environment.BeamEnvironment.configuration;
import static diarsid.beam.core.base.control.io.base.interaction.VariantConversions.stringsToVariants;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.support.objects.Pools.pools;

/**
 *
 * @author Diarsid
 */
public class AnalyzeTestTwo {
    
    
    
    private static WeightAnalyzeReal analyzeInstance;
    private static int totalVariantsQuantity;
    private static long start;
    private static long stop;
    
    private WeightAnalyzeReal analyze;
    private boolean expectedToFail;
    private String pattern;
    private List<String> variants;
    private List<String> expected;
    private Variants weightedVariants;
    
    public AnalyzeTestTwo() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        Similarity similarity = new Similarity(configuration());
        analyzeInstance = new WeightAnalyzeReal(configuration(), similarity, pools());
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
        Optional<Pool<AnalyzeUnit>> pool = pools().poolOf(AnalyzeUnit.class);
        if ( pool.isPresent() ) {
            Pool<AnalyzeUnit> c = pool.get();
            AnalyzeUnit analyzeData = c.give();
        }
    }
    
    @Before
    public void setUp() {
        this.analyze = analyzeInstance;
    }
    
    @After
    public void tearDown() {
        this.analyze.resultsLimitToDefault();
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
        weightedVariants = this.analyze.weightVariants(pattern, stringsToVariants(variants));
        
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
                actualVariant = weightedVariants.current().value();
                
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
                variantsWithWeight.add("\n" + weightedVariants.current().value() + " is much better than next: " + weightedVariants.current().weight());
            } else {
                variantsWithWeight.add("\nnext candidates are similar: ");                
                weightedVariants.nextSimilarVariants()
                        .stream()
                        .forEach(candidate -> {
                            variantsWithWeight.add("\n  - " + candidate.value() + " : " + candidate.weight());
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
