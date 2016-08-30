/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoActionChoice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import static diarsid.beam.core.modules.executor.commandscache.ActionChoice.formulateChoiceFor;
import static diarsid.beam.core.modules.executor.commandscache.FakeChoiceProducer.request;

/**
 *
 * @author Diarsid
 */
public class ActionsResolverTest {
    
    private ActionsResolver resolver;
    private DaoActionChoice dao;
    private IoInnerModule ioEngine;
           
    public ActionsResolverTest() {
        dao = mock(DaoActionChoice.class);
        ioEngine = mock(IoInnerModule.class);
        resolver = new ActionsResolver(ioEngine, dao);
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testResolveAction_resolvedFromMemory() {
        List<String> variants = Arrays.asList(new String[] {
            "run netbeans",
            "open netbeans-project"
        });
        String actionArg = "netb";
        String madeChocie = "run netbeans";
        ActionRequest request = request(actionArg, variants);
        
        when(dao.getChoiceFor(request)).thenReturn(madeChocie);
        when(ioEngine.resolveVariantsWithExternalIO("action", variants)).thenReturn(1);
        
        String result = resolver.resolve(request);
        assertEquals(madeChocie, result);
        
        verify(dao).getChoiceFor(request);
        verify(ioEngine, never()).resolveVariantsWithExternalIO("action?", variants);
    }
    
    @Test
    public void testResolveAction_resolvedFromIo_saveChoice() {
        List<String> variants = Arrays.asList(new String[] {
            "run netbeans",
            "open netbeans-project"
        });
        String actionArg = "netb";
        String madeChoice = "run netbeans";
        ActionRequest request = request(actionArg, variants);
        ActionChoice choice = formulateChoiceFor(request, madeChoice);
        
        when(dao.getChoiceFor(request)).thenReturn("");
        when(ioEngine.resolveVariantsWithExternalIO("action?", variants)).thenReturn(1);
        when(ioEngine.askUserYesOrNo("Use this choice in future?")).thenReturn(true);
        
        String result = resolver.resolve(request);
        
        verify(ioEngine).resolveVariantsWithExternalIO("action?", variants);
        verify(ioEngine).askUserYesOrNo("Use this choice in future?");
        verify(dao).saveChoice(choice);
        
        assertEquals(madeChoice, result);
    }

}