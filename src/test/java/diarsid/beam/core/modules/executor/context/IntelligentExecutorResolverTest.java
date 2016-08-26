/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.context;

import diarsid.beam.core.modules.executor.context.SmartAmbiguityResolver;
import diarsid.beam.core.modules.executor.context.ContextChoiceSavingCallback;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoExecutorIntelligentChoices;
import diarsid.beam.core.modules.executor.workflow.CommandChoice;
import diarsid.beam.core.modules.executor.workflow.CurrentCommandState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 *
 * @author Diarsid
 */
public class IntelligentExecutorResolverTest {
    
    // common class inner functional objects
    private final List<String> yesOrNo = new ArrayList<>(); {
        yesOrNo.add("yes");
        yesOrNo.add("no");
    }    
    
    // command for test cases
    private final String case_open_1 = "open j in eng";
    private final List<String> variants_for_j = new ArrayList<>(); {
        variants_for_j.add("java");
        variants_for_j.add("node.js_engine");
        variants_for_j.add("jython_engine");
    }
    private final List<String> variants_for_j_unable_to_guess = new ArrayList<>(); {
        variants_for_j_unable_to_guess.add("java");
        variants_for_j_unable_to_guess.add("nodejs");
        variants_for_j_unable_to_guess.add("javae");
        variants_for_j_unable_to_guess.add("jsa");
    }
    private final List<String> variants_for_eng = new ArrayList<>(); {
        variants_for_eng.add("engines");
        variants_for_eng.add("english");
    }
    private final CurrentCommandState case_open_1_state; {
        CommandChoice case_open_1_choice_1 = 
                new CommandChoice("j", "java", 0);
        CommandChoice case_open_1_choice_2 = 
                new CommandChoice("eng", "engines", 1);
        List<CommandChoice> choices = new ArrayList<>();
        choices.add(case_open_1_choice_1);
        choices.add(case_open_1_choice_2);
        case_open_1_state = new CurrentCommandState(case_open_1, choices);
    }
    
    
    // tested object
    private SmartAmbiguityResolver testedResolver;
    
    // tested resolver dependencies (mocks)
    private DaoExecutorIntelligentChoices choicesDao;
    private IoInnerModule ioEngine;
    private ContextChoiceSavingCallback contextCallback;

    public IntelligentExecutorResolverTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        DataModule data = mock(DataModule.class);
        this.choicesDao = mock(DaoExecutorIntelligentChoices.class);
        this.ioEngine = mock(IoInnerModule.class);
        this.contextCallback = mock(ContextChoiceSavingCallback.class);
        
        when(data.getIntellChoiceDao()).thenReturn(this.choicesDao);
        
        this.testedResolver = new SmartAmbiguityResolver(data, this.ioEngine);
    }

    @After
    public void tearDown() {
    }
            
    @Test
    public void testRememberIfAutomatically() {        
        testedResolver.remember(case_open_1_state);        
        verify(this.choicesDao).saveChoiceForCommandAndItsPart(case_open_1_state);        
    }

    @Test
    public void testRememberIfRememberAutomaticallySetFalse() {
        
        when(ioEngine.resolveVariantsWithExternalIO(
                "remember your choice for this command?", yesOrNo))
                .thenReturn(1);
        testedResolver.setRememberChoiceAutomatically(false);
        testedResolver.remember(case_open_1_state);      
        verify(ioEngine).resolveVariantsWithExternalIO(
                "remember your choice for this command?", yesOrNo);
        verify(this.choicesDao).saveChoiceForCommandAndItsPart(case_open_1_state);
    }
    
    @Test
    public void testRememberIfRememberAutomaticallySetTrue() {        
        testedResolver.setRememberChoiceAutomatically(true);
        testedResolver.remember(case_open_1_state);  
        verify(ioEngine, never()).resolveVariantsWithExternalIO(
                "remember your choice for this command?", yesOrNo);
        verify(this.choicesDao).saveChoiceForCommandAndItsPart(case_open_1_state);
    }

    @Test
    public void testResolve_j_true() {
        int result = testedResolver.resolve(
                "?", case_open_1, 0, "j", variants_for_j, contextCallback);
        verify(choicesDao, never()).getChoiceForCommandPart(
                case_open_1, 0, "j");
        verify(ioEngine, never()).resolveVariantsWithExternalIO(
                "?", variants_for_j);
        verify(contextCallback).doNotSaveThisChoice();
        verify(contextCallback, never()).saveThisChoice();
        assertEquals(1, result);  
        assertEquals("java", variants_for_j.get(result-1));
    }
    
    @Test
    public void testResolve_j_true_unable_to_guess() {
        when(ioEngine.resolveVariantsWithExternalIO("?", variants_for_j_unable_to_guess))
                .thenReturn(1);
        when(choicesDao.getChoiceForCommandPart(case_open_1, 0, "j")).thenReturn("");
        int result = testedResolver.resolve(
                "?", case_open_1, 0, "j", variants_for_j_unable_to_guess, contextCallback);
        verify(choicesDao).getChoiceForCommandPart(
                case_open_1, 0, "j");
        verify(ioEngine).resolveVariantsWithExternalIO(
                "?", variants_for_j_unable_to_guess);
        verify(contextCallback, never()).doNotSaveThisChoice();
        verify(contextCallback).saveThisChoice();
        assertEquals(1, result);  
        assertEquals("java", variants_for_j_unable_to_guess.get(result-1));
    }
    
    @Test
    public void testResolve_j_false() {
        int result = testedResolver.resolve(
                "?", case_open_1, 0, "j", variants_for_j, contextCallback);
        verify(choicesDao, never()).getChoiceForCommandPart(case_open_1, 0, "j");
        verify(contextCallback).doNotSaveThisChoice();
        verify(contextCallback, never()).saveThisChoice();
        verify(ioEngine, never()).resolveVariantsWithExternalIO("?", variants_for_j);
        assertNotEquals(2, result);
        assertNotEquals("jython", variants_for_j.get(result-1));
    }
    
    @Test
    public void testResolve_eng_daoCannotResolve() {
        when(ioEngine.resolveVariantsWithExternalIO("?", variants_for_eng))
                .thenReturn(1);
        when(choicesDao.getChoiceForCommandPart(case_open_1, 1, "eng")).thenReturn("");
        int result = testedResolver.resolve(
                "?", case_open_1, 1, "eng", variants_for_eng, contextCallback);
        verify(contextCallback, never()).doNotSaveThisChoice();
        verify(contextCallback).saveThisChoice();
        verify(choicesDao).getChoiceForCommandPart(case_open_1, 1, "eng");
        verify(ioEngine).resolveVariantsWithExternalIO("?", variants_for_eng);
        assertEquals(1, result);        
    }
    
    @Test
    public void testResolve_eng_daoMustResolve() {
        when(ioEngine.resolveVariantsWithExternalIO("?", variants_for_eng))
                .thenReturn(1);
        when(choicesDao.getChoiceForCommandPart(case_open_1, 1, "eng")).thenReturn("engines");
        int result = testedResolver.resolve(
                "?", case_open_1, 1, "eng", variants_for_eng, contextCallback);
        verify(contextCallback).doNotSaveThisChoice();
        verify(choicesDao, never()).saveChoiceForCommandAndItsPart(case_open_1_state);
        verify(choicesDao).getChoiceForCommandPart(case_open_1, 1, "eng");
        verify(ioEngine, never()).resolveVariantsWithExternalIO("?", variants_for_eng);
        assertEquals(1, result);        
    }

    @Test
    public void testGetAllChoices() {
        List<CurrentCommandState> commands = new ArrayList<>();
        commands.add(case_open_1_state);
        when(choicesDao.getAllChoices()).thenReturn(commands);
        List<String> expected = new ArrayList<>();
        expected.add(case_open_1 + " -> j->java eng->engines ");
        when(choicesDao.formatCommandsForOutput(commands)).thenReturn(expected);
        List<String> output = testedResolver.getAllChoices();
        assertEquals(
                case_open_1 + " -> j->java eng->engines ", output.get(0));
    }

    @Test
    public void testSetActive_true() {
        testedResolver.setActive(true);
        verify(ioEngine).reportMessage("Intelligent command resolving enabled.");
    }
    
    @Test
    public void testSetActive_false() {
        testedResolver.setActive(false);
        verify(ioEngine).reportMessage("Intelligent command resolving disabled.");
    }

    @Test
    public void testDeleteChoicesForCommand_noCommandsFound() {        
        when(choicesDao.getChoicesWhereCommandLike("ja")).thenReturn(new ArrayList<>());
        testedResolver.deleteChoicesForCommand("ja");
    }
    
    @Test
    public void testDeleteChoicesForCommand_oneCommandFound() {
        List<CurrentCommandState> foundCommands = new ArrayList<>();
        foundCommands.add(case_open_1_state);
        when(choicesDao.getChoicesWhereCommandLike("ja")).thenReturn(foundCommands);
        testedResolver.deleteChoicesForCommand("ja");
        verify(choicesDao).deleteChoicesForCommand(case_open_1);
    }
    
    @Test
    public void testDeleteChoicesForCommand_moreCommandsFound() {
        List<CurrentCommandState> foundCommands = new ArrayList<>();
        foundCommands.add(case_open_1_state);
        String oneMoreCommand = "op jav in tes";
        CommandChoice choice1 = new CommandChoice("jav", "java_projects", 0);
        CommandChoice choice2 = new CommandChoice("tes", "test_projects", 1);
        List<CommandChoice> choices = new ArrayList<>();
        choices.add(choice1);
        choices.add(choice2);
        CurrentCommandState oneMoreState = new CurrentCommandState(
                oneMoreCommand, choices);
        foundCommands.add(oneMoreState);
        when(choicesDao.getChoicesWhereCommandLike("ja")).thenReturn(foundCommands);
        List<String> variants = new ArrayList<>();
        variants.add(case_open_1 + " -> j->java eng->engines ");
        variants.add(oneMoreCommand + " -> jav->java_projects tes->test_projects ");
        when(choicesDao.formatCommandsForOutput(foundCommands)).thenReturn(variants);
        when(ioEngine.resolveVariantsWithExternalIO(
                "Which command delete from memory?", variants))
                .thenReturn(2);
        when(choicesDao.deleteChoicesForCommand(oneMoreCommand))
                .thenReturn(true);
        boolean deleted = testedResolver.deleteChoicesForCommand("ja");
        verify(ioEngine).resolveVariantsWithExternalIO(
                "Which command delete from memory?", variants);
        verify(choicesDao).deleteChoicesForCommand(oneMoreCommand);
        assertTrue(deleted);
    }
}