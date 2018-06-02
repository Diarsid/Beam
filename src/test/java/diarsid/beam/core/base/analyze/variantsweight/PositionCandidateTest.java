/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Diarsid
 */
public class PositionCandidateTest {
    
    private final PositionCandidate positionCandidate = new PositionCandidate();
    
    public PositionCandidateTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        positionCandidate.clear();
    }

    @Test
    public void testMutate_1() {
        positionCandidate.mutate(12, 1, 1, 1);
        positionCandidate.mutate(3, 5, 1, 2);
        
        assertThat(positionCandidate.position(), equalTo(12));
    }
    
    @Test
    public void testMutate_2() {
        positionCandidate.mutate(12, 2, 1, 1);
        positionCandidate.mutate(3, 1, 1, 2);
        
        assertThat(positionCandidate.position(), equalTo(3));
    }

    @Test
    public void testMutate_3() {
        positionCandidate.mutate(12, 1, 2, 1);
        positionCandidate.mutate(3, 3, 1, 1);
        
        assertThat(positionCandidate.position(), equalTo(12));
    }
    
}
