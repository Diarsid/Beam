/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package old.diarsid.beam.core.modules.data.daos;

import old.diarsid.beam.core.modules.data.daos.H2DaoActionChoice;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import old.diarsid.beam.core.modules.IoInnerModule;

import old.diarsid.beam.core.modules.data.base.H2TestDataBase;
import old.diarsid.beam.core.modules.data.base.TestDataBase;

import diarsid.beam.core.modules.executor.commandscache.ActionChoice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import static diarsid.beam.core.modules.data.base.builder.Tables.actionChoicesTableScript;
import static diarsid.beam.core.modules.executor.commandscache.FakeChoiceProducer.choice;
import static diarsid.beam.core.modules.executor.commandscache.FakeChoiceProducer.request;

/**
 *
 * @author Diarsid
 */
//@Ignore
public class H2DaoActionChoiceTest {
    
    private static final String H2_in_memory_test_base_URL = 
            "jdbc:h2:mem:beamTest;DB_CLOSE_DELAY=-1";
           
    private static TestDataBase testBase; 
    private static IoInnerModule ioEngine;
    private static H2DaoActionChoice dao;

    public H2DaoActionChoiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        testBase = new H2TestDataBase(H2_in_memory_test_base_URL);
        testBase.setupRequiredTable(actionChoicesTableScript());
        ioEngine = mock(IoInnerModule.class);
        dao = new H2DaoActionChoice(ioEngine, testBase);
    }

    @AfterClass
    public static void tearDownClass() {
        testBase.disconnect();
    }
    
    private static void rollbackBase() {        
        try {
            Connection con = testBase.connect();
            Statement st = con.createStatement();
            st.executeUpdate("DELETE FROM action_choices");
            st.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();            
        }
    }

    /**
     * Test of saveChoice method, of class H2DaoActionChoice.
     */
    @Test
    public void testSaveChoice() {
        List<String> variants = Arrays.asList(new String[] {
            "run netbeans",
            "open netbeans-project"
        });
        String actionArg = "netb";
        String madeChocie = "run netbeans";
        
        ActionChoice choice = choice(actionArg, variants, madeChocie);
        boolean saved = dao.saveChoice(choice);
        
        assertTrue(saved);
        rollbackBase();
    }
    
    @Test
    public void testSaveChoice_rewriteExisted() {
        List<String> variants = Arrays.asList(new String[] {
            "run netbeans",
            "open netbeans-project"
        });
        String actionArg = "netb";
        String madeChocie = "run netbeans";
        
        ActionChoice choice = choice(actionArg, variants, madeChocie);
        boolean saved = dao.saveChoice(choice);
        assertTrue(saved);
        
        List<String> variants2 = Arrays.asList(new String[] {
            "run netbeans",
            "open netbeans-project",
            "see netbeans"
        });
        String madeChocie2 = "see netbeans";
        
        ActionChoice choice2 = choice(actionArg, variants2, madeChocie2);
        boolean saved2 = dao.saveChoice(choice2);
        assertTrue(saved2);
        
        String obtained = dao.getChoiceFor(request(actionArg, variants2));
        
        assertEquals(madeChocie2, obtained);
        rollbackBase();
    }

    /**
     * Test of getChoiceFor method, of class H2DaoActionChoice.
     */
    @Test
    public void testGetChoiceFor() {
        List<String> variants = Arrays.asList(new String[] {
            "run netbeans",
            "open netbeans-project"
        });
        String actionArg = "netb";
        String madeChocie = "run netbeans";
        
        ActionChoice choice = choice(actionArg, variants, madeChocie);
        boolean saved = dao.saveChoice(choice);
        assertTrue(saved);
        
        String obtained = dao.getChoiceFor(request(actionArg, variants));
        assertEquals("run netbeans", obtained);
        rollbackBase();
    }

    /**
     * Test of deleteChoiceFor method, of class H2DaoActionChoice.
     */
    @Test
    public void testDeleteChoiceFor() {
        List<String> variants = Arrays.asList(new String[] {
            "run netbeans",
            "open netbeans-project"
        });
        String actionArg = "netb";
        String madeChocie = "run netbeans";
        
        ActionChoice choice = choice(actionArg, variants, madeChocie);
        boolean saved = dao.saveChoice(choice);
        assertTrue(saved);
        
        boolean deleted = dao.deleteChoiceFor(actionArg);
        assertTrue(deleted);
        rollbackBase();
    }

}