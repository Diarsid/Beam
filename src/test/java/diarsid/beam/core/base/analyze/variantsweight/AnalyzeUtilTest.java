/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.analyze.variantsweight;

import java.util.List;

import org.junit.After;
import org.junit.Test;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static diarsid.support.objects.Pools.giveBackToPool;
import static diarsid.beam.core.base.analyze.variantsweight.AnalyzeUtil.processCluster;

/**
 *
 * @author Diarsid
 */
public class AnalyzeUtilTest {
    
    private Cluster cluster;
    
    private void process(Integer... orders) {
        List<Integer> ordersList = stream(orders).collect(toList());
        cluster = calculateCluster(ordersList, 0, orders.length);
    }
    
    @After
    public void tearDown() {
        giveBackToPool(cluster);
    }
    
    @Test
    public void test__0_1_1_m2_0_0_0() {
        process(0, 1, 1, -2, 0, 0, 0);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(true));
    }
    
    @Test
    public void test__0_0_2_m1_m1() {
        process(0, 0, 2, -1, -1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(true));
    }
    
    @Test
    public void test__0_0_m1_m1_2() {
        process(0, 0, -1, -1, 2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(true));
    }
    
    @Test
    public void test__m1_m1_2_0_0() {
        process(-1, -1, 2, 0, 0);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(true));
    }
    
    @Test
    public void test__2_m1_m1() {
        process(2, -1, -1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(true));
    }

    @Test
    public void test__m1_m1_2() {
        process(-1, -1, 2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));        
    }

    @Test
    public void test__0_m1_m1_2_0() {
        process(0, -1, -1, 2, 0);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));        
    }

    @Test
    public void test__3_m2_1_m3() {
        process(3, -2, 1, -3);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(9));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));        
    }

    @Test
    public void test__3_m2_2_m3() {
        process(3, -2, 2, -3);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(10));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__1_m1() {
        process(1, -1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(1));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));
    }

    @Test
    public void test__m1_m2() {
        process(-1, -2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(1));
        assertThat(cluster.ordersDiffMean(), equalTo(-1));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__m1_m1() {
        process(-1, -1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(-1));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__3_m1() {
        process(3, -1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(4));
        assertThat(cluster.ordersDiffMean(), equalTo(1));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__m2_m1() {
        process(-2, -1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(1));
        assertThat(cluster.ordersDiffMean(), equalTo(-1));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));        
    }

    @Test
    public void test__2_m1() {
        process(2, -1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(3));
        assertThat(cluster.ordersDiffMean(), equalTo(1));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));        
    }

    @Test
    public void test__m2_m4() {
        process(-2, -4);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(1));
        assertThat(cluster.ordersDiffMean(), equalTo(-3));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));
    }

    @Test
    public void test__m4_m2() {
        process(-4, -2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(1));
        assertThat(cluster.ordersDiffMean(), equalTo(-3));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));
    }

    @Test
    public void test__1_m1_m5() {
        process(1, -1, -5);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(7));
        assertThat(cluster.ordersDiffMean(), equalTo(-2));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(false));
    }
    
    @Test
    public void test__3_3_2_2_5_3() {
        process(3, 3, 2, 2, 5, 3);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(3));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));      
    }
    
    @Test
    public void test__3_3_5_2_2_3() {
        process(3, 3, 5, 2, 2, 3);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(3));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));      
    }

    @Test
    public void test__1_1_1_1_m4() {
        process(1, 1, 1, 1, -4);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat("shifts expected, but not present", cluster.hasOrdersDiffShifts(), equalTo(true));
    }

    @Test
    public void test__m1_1_1() {
        process(-1, 1, 1);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(1));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__m1_1_0() {
        process(-1, 1, 0);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__m1_m1_m1_3_m2() {
        process(-1, -1, -1, 3, -2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(2));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(true));
    }

    @Test
    public void test__m1_m1_m1_m1_m2() {
        process(-1, -1, -1, -1, -2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(1));
        assertThat(cluster.ordersDiffMean(), equalTo(-1));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__3_4_2() {
        process(3, 4, 2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(3));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__3_1_m2_m2() {
        process(3, 1, -2, -2);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(false));
        assertThat(cluster.ordersDiffSumAbs(), equalTo(8));
        assertThat(cluster.ordersDiffMean(), equalTo(0));
        assertThat(cluster.hasOrdersDiffShifts(), equalTo(false));
    }

    @Test
    public void test__6_4_5_5() {
        process(6, 4, 5, 5);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(5));
        assertThat("shifts not expected, but present", cluster.hasOrdersDiffShifts(), equalTo(false));       
    } 

    @Test
    public void test__5_4_6_5_5() {
        process(5, 4, 6, 5, 5);
        
        assertThat(cluster.haveOrdersDiffCompensations(), equalTo(true));
        assertThat(cluster.hasOrdersDiff(), equalTo(false));
        assertThat(cluster.ordersDiffMean(), equalTo(5));
        assertThat("shifts not expected, but present", cluster.hasOrdersDiffShifts(), equalTo(false));       
    } 
}
