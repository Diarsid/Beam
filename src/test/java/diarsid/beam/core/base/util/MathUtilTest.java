/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import static diarsid.beam.core.base.util.MathUtil.adjustBetween;

/**
 *
 * @author Diarsid
 */
public class MathUtilTest {

    public MathUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of adjustBetween method, of class IntUtil.
     */
    @Test
    public void testAdjustBetween() {
        int toAdjust1 = 5;
        assertEquals(5, adjustBetween(toAdjust1, 0, 9));
        
        int toAdjust2 = -1;
        assertEquals(0, adjustBetween(toAdjust2, 0, 9));
        
        int toAdjust3 = 12;
        assertEquals(9, adjustBetween(toAdjust3, 0, 9));
    }

}