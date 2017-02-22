/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLIKE;


/**
 *
 * @author Diarsid
 */
public class SqlUtilTest {

    public SqlUtilTest() {
    }

    /**
     * Test of composeWildcarded method, of class SqlUtil.
     */
    @Test
    public void testComposeWildcarded() {
        String result = multipleLowerLIKE("obj_name", 3, AND);
        
        assertEquals(" ( LOWER(obj_name) LIKE ? ) AND ( LOWER(obj_name) LIKE ? ) AND ( LOWER(obj_name) LIKE ? ) ", result);
    }

}