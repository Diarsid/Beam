/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.base.H2TestDataBase;
import diarsid.beam.core.modules.data.base.TestDataBase;
import diarsid.beam.core.modules.executor.CommandChoice;
import diarsid.beam.core.modules.executor.CurrentCommandState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import static diarsid.beam.core.modules.data.base.builder.Tables.commandChoicesTableScript;

/**
 *
 * @author Diarsid
 */
public class H2DaoExecutorIntelligentChoicesTest {
    
    private final static String H2_in_memory_test_base_URL = 
            "jdbc:h2:mem:beamTest;DB_CLOSE_DELAY=-1";
    private final static String case_open_1 = "open j in eng";
    private final static CurrentCommandState case_open_1_state; 
    static {
        CommandChoice case_open_1_choice_1 = 
                new CommandChoice("j", "java", 0);
        CommandChoice case_open_1_choice_2 = 
                new CommandChoice("eng", "engines", 1);
        List<CommandChoice> choices = new ArrayList<>();
        choices.add(case_open_1_choice_1);
        choices.add(case_open_1_choice_2);
        case_open_1_state = new CurrentCommandState(case_open_1, choices);
    }
    private final static String case_open_2 = "op ir in fi";
    private final static CurrentCommandState case_open_2_state; 
    static {        
        CommandChoice choice_1 = 
                new CommandChoice("ir", "iron_man", 0);
        CommandChoice choice_2 = 
                new CommandChoice("fi", "films", 1);
        List<CommandChoice> choices = new ArrayList<>();
        choices.add(choice_1);
        choices.add(choice_2);
        case_open_2_state = new CurrentCommandState(case_open_2, choices);
    }
    
    private static TestDataBase testBase;    
    private static H2DaoExecutorIntelligentChoices testedDao;
    private static IoInnerModule ioEngine;

    @BeforeClass
    public static void setUpClass() {
        testBase = new H2TestDataBase(H2_in_memory_test_base_URL);
        testBase.setupRequiredTable(commandChoicesTableScript());
        ioEngine = mock(IoInnerModule.class);
        testedDao = new H2DaoExecutorIntelligentChoices(ioEngine, testBase);
        testedDao.saveChoiceForCommandAndItsPart(case_open_1_state);
        String choice = testedDao.getChoiceForCommandPart(case_open_1, 0, "j");
        System.out.println(choice);
    }

    @AfterClass
    public static void tearDownClass() {
        testBase.disconnect();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getChoiceForCommandPart method, of class H2DaoExecutorIntelligentChoices.
     */
    @Test
    public void testGetChoiceForCommandPart() {
        String choice = testedDao.getChoiceForCommandPart(case_open_1, 0, "j");
        assertEquals("java", choice);
    }

    /**
     * Test of saveChoiceForCommandAndItsPart method, of class H2DaoExecutorIntelligentChoices.
     */
    @Test
    public void testSaveChoiceForCommandAndItsPart() {
        testedDao.saveChoiceForCommandAndItsPart(case_open_2_state);
        String choice_0 = testedDao.getChoiceForCommandPart(
                case_open_2, 0, "ir");
        String choice_1 = testedDao.getChoiceForCommandPart(
                case_open_2, 1, "fi");
        assertEquals("iron_man", choice_0);
        assertEquals("films", choice_1);
    }

    /**
     * Test of deleteChoicesForCommand method, of class H2DaoExecutorIntelligentChoices.
     */
    @Test
    public void testDeleteChoicesForCommand() {
        boolean deleted = testedDao.deleteChoicesForCommand(case_open_2);
        String choice_0 = testedDao.getChoiceForCommandPart(
                case_open_2, 0, "ir");
        assertTrue(deleted);
        assertEquals("", choice_0);
        // restore
        testedDao.saveChoiceForCommandAndItsPart(case_open_2_state);
    }

    /**
     * Test of getAllChoices method, of class H2DaoExecutorIntelligentChoices.
     */
    @Test
    public void testGetAllChoices() {
        List<CurrentCommandState> commands = testedDao.getAllChoices();
        assertTrue(commands.contains(case_open_1_state));
        assertTrue(commands.contains(case_open_2_state));
    }

    /**
     * Test of getChoicesWhereCommandLike method, of class H2DaoExecutorIntelligentChoices.
     */
    @Test
    public void testGetChoicesWhereCommandLike() {
        List<CurrentCommandState> commands = 
                testedDao.getChoicesWhereCommandLike("jav");
        assertEquals(case_open_1_state.getCommandString(), commands.get(0).getCommandString());
    }
    
    /**
     * Test of formatCommandsForOutput method, of class H2DaoExecutorIntelligentChoices.
     */
    @Test
    public void testFormatCommandsForOutput() {
        List<String> formatted = testedDao.formatCommandsForOutput(
                testedDao.getAllChoices());
        assertEquals(
                "open j in eng -> j->java eng->engines ", formatted.get(0));
    }
}