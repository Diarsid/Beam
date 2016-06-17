/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

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
import diarsid.beam.core.modules.data.DaoExecutorConsoleCommands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 *
 * @author Diarsid
 */
public class CommandsIntelligentCacheTest {
    
    private final IoInnerModule ioEngine;
    private final DaoExecutorConsoleCommands dao;
    private final CommandsIntelligentCache cache;
    
    {
        ioEngine = mock(IoInnerModule.class);
        dao = mock(DaoExecutorConsoleCommands.class);
        cache = new CommandsIntelligentCache(ioEngine, dao);
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
     * Test of addCommand method, of class CommandsIntelligentCache.
     */
    @Test
    public void testAddCommand_List() {
    }

    /**
     * Test of addCommand method, of class CommandsIntelligentCache.
     */
    @Test
    public void testAddCommand_String() {
    }

    /**
     * Test of getPatternCommandForExecution method, of class CommandsIntelligentCache.
     */
    @Test
    public void testGetPatternCommandForExecution() {
        SortedMap<String, String> result = new TreeMap<>();
        result.put("open java in eng", "open java in eng");
        result.put("open java in engines", "open java in engines");
        when(dao.getImprovedCommandsForPattern("j-eng")).thenReturn(result);
        String command = cache.getPatternCommandForExecution("j-eng");
        verify(dao).getImprovedCommandsForPattern("j-eng");        
        assertEquals("open java in eng", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_call_start_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        
        when(dao.getImprovedCommandsForPattern("tomc")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tomc");
        
        verify(dao).getImprovedCommandsForPattern("tomc");        
        assertEquals("call tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_call_are_equal_start_differ() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomEE", "start tomEE");
        rawCachedCommands.put("call tomcat", "call tomcat");
        List<String> variants = new ArrayList<>();
        variants.add("call tomcat");
        variants.add("start tomEE");
        
        when(dao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(ioEngine.resolveVariantsWithExternalIO("action?", variants)).thenReturn(2);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(dao).getImprovedCommandsForPattern("tom");
        verify(ioEngine).resolveVariantsWithExternalIO("action?", variants);        
        assertEquals("start tomEE", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_start_are_equal_call_differ() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomEE", "call tomEE");
        List<String> variants = new ArrayList<>();
        variants.add("call tomEE");
        variants.add("start tomcat");
        
        
        when(dao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(ioEngine.resolveVariantsWithExternalIO("action?", variants)).thenReturn(2);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(dao).getImprovedCommandsForPattern("tom");
        verify(ioEngine).resolveVariantsWithExternalIO("action?", variants);        
        assertEquals("start tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_call_start_are_equal_run_differ() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomEE", "run tomEE");
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        List<String> variants = new ArrayList<>();        
        variants.add("call tomcat");
        variants.add("run tomEE");
        
        when(dao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        when(ioEngine.resolveVariantsWithExternalIO("action?", variants)).thenReturn(2);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(dao).getImprovedCommandsForPattern("tom");
        verify(ioEngine).resolveVariantsWithExternalIO("action?", variants);        
        assertEquals("run tomEE", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_call_start_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("start tomcat", "start tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        
        when(dao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(dao).getImprovedCommandsForPattern("tom");
        verifyZeroInteractions(ioEngine);   
        assertEquals("call tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_call_run_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("call tomcat", "call tomcat");
        
        when(dao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(dao).getImprovedCommandsForPattern("tom");
        verifyZeroInteractions(ioEngine);  
        assertEquals("call tomcat", command);
    }
    
    @Test
    public void testGetPatternCommandForExecution_run_start_are_equal() {
        SortedMap<String, String> rawCachedCommands = new TreeMap<>();
        rawCachedCommands.put("run tomcat", "run tomcat");
        rawCachedCommands.put("start tomcat", "start tomcat");
        
        when(dao.getImprovedCommandsForPattern("tom")).thenReturn(rawCachedCommands);
        
        String command = cache.getPatternCommandForExecution("tom");
        
        verify(dao).getImprovedCommandsForPattern("tom");
        verifyZeroInteractions(ioEngine);  
        assertEquals("start tomcat", command);
    }

}