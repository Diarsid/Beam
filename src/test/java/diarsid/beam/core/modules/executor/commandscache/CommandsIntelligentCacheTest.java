/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.commandscache;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.data.DaoActionChoice;
import diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import static diarsid.beam.core.modules.executor.commandscache.ActionRequest.actionRequestOf;

/**
 *
 * @author Diarsid
 */
public class CommandsIntelligentCacheTest {
    
    private final IoInnerModule ioEngine;
    private final DaoExecutorConsoleCommands consoleDao;
    private final DaoActionChoice actionsDao;
    private final SmartConsoleCommandsCacheWorker cache;
    
    {
        ioEngine = mock(IoInnerModule.class);
        consoleDao = mock(DaoExecutorConsoleCommands.class);
        actionsDao = mock(DaoActionChoice.class);
        ActionsResolver actionsResolver = new ActionsResolver(
                ioEngine, actionsDao);
        cache = new SmartConsoleCommandsCacheWorker(
                ioEngine, actionsResolver, consoleDao);
    }
    

    public CommandsIntelligentCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of addCommand method, of class SmartConsoleCommandsCacheWorker.
     */
    @Test
    public void testAddCommand_List() {
    }

    /**
     * Test of addCommand method, of class SmartConsoleCommandsCacheWorker.
     */
    @Test
    public void testAddCommand_String() {
    }

    /**
     * Test of getPatternCommandForExecution method, of class SmartConsoleCommandsCacheWorker.
     */
    @Test
    public void testGetPatternCommandForExecution() {
        SortedMap<String, String> result = new TreeMap<>();
        result.put("open java in eng", "open java in eng");
        result.put("open java in engines", "open java in engines");
        when(consoleDao.getImprovedCommandsForPattern("j-eng")).thenReturn(result);
        String command = cache.getPatternCommandForExecution("j-eng");
        verify(consoleDao).getImprovedCommandsForPattern("j-eng");        
        assertEquals("open java in eng", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_call_start_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        
        when(consoleDao.getImprovedCommandsForPattern("tomc")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tomc");
        
        verify(consoleDao).getImprovedCommandsForPattern("tomc");        
        assertEquals("call tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_call_are_equal_start_differ_resolvedFromMemory() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomEE", "start tomEE");
        rawCachedCommands.put("call tomcat", "call tomcat");
        List<String> variants = new ArrayList<>();
        variants.add("call tomcat");
        variants.add("start tomEE");
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(ioEngine.resolveVariants("action?", variants)).thenReturn(2);
        when(actionsDao.getChoiceFor(actionRequestOf("tom", variants))).thenReturn("start tomEE");
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verify(ioEngine, never()).resolveVariants("action?", variants);
        verify(actionsDao).getChoiceFor(actionRequestOf("tom", variants));        
        assertEquals("start tomEE", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_call_are_equal_start_differ_resolvedByIO() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomEE", "start tomEE");
        rawCachedCommands.put("call tomcat", "call tomcat");
        List<String> variants = new ArrayList<>();
        variants.add("call tomcat");
        variants.add("start tomEE");
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(ioEngine.resolveVariants("action?", variants)).thenReturn(2);
        when(actionsDao.getChoiceFor(actionRequestOf("tom", variants))).thenReturn("");
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verify(ioEngine).resolveVariants("action?", variants);
        verify(actionsDao).getChoiceFor(actionRequestOf("tom", variants));        
        assertEquals("start tomEE", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_start_are_equal_call_differ_resolvedFromMemory() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomEE", "call tomEE");
        List<String> variants = new ArrayList<>();
        variants.add("call tomEE");
        variants.add("start tomcat");
        
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(actionsDao.getChoiceFor(actionRequestOf("tom", variants))).thenReturn("start tomcat");
        when(ioEngine.resolveVariants("action?", variants)).thenReturn(2);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verify(actionsDao).getChoiceFor(actionRequestOf("tom", variants));
        verify(ioEngine, never()).resolveVariants("action?", variants);        
        assertEquals("start tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_start_are_equal_call_differ_resolvedByIO() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomEE", "call tomEE");
        List<String> variants = new ArrayList<>();
        variants.add("call tomEE");
        variants.add("start tomcat");
        
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(actionsDao.getChoiceFor(actionRequestOf("tom", variants))).thenReturn("");
        when(ioEngine.resolveVariants("action?", variants)).thenReturn(2);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verify(actionsDao).getChoiceFor(actionRequestOf("tom", variants));
        verify(ioEngine).resolveVariants("action?", variants);        
        assertEquals("start tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_call_start_are_equal_run_differ_resolvedFromMemory() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomEE", "run tomEE");
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        List<String> variants = new ArrayList<>();        
        variants.add("call tomcat");
        variants.add("run tomEE");
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(actionsDao.getChoiceFor(actionRequestOf("tom", variants))).thenReturn("run tomEE");
        when(ioEngine.resolveVariants("action?", variants)).thenReturn(2);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verify(ioEngine, never()).resolveVariants("action?", variants);    
        verify(actionsDao).getChoiceFor(actionRequestOf("tom", variants));
        assertEquals("run tomEE", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_call_start_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verifyZeroInteractions(ioEngine);   
        assertEquals("call tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_call_run_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verifyZeroInteractions(ioEngine);  
        assertEquals("call tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_start_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomcat", "start tomcat");
        
        when(consoleDao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(consoleDao).getImprovedCommandsForPattern("tom");
        verifyZeroInteractions(ioEngine);  
        assertEquals("start tomcat", command);
    }

}