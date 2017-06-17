/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static diarsid.beam.core.base.util.CollectionsUtils.shrink;

/**
 *
 * @author Diarsid
 */
public class CollectionsUtilsTest {
    
    public CollectionsUtilsTest() {
    }

    @Test
    public void testShrink() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        
        shrink(list, 2);
        
        assertEquals(2, list.size());
    }
    
}
