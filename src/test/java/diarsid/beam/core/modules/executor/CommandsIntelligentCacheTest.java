/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor;

import java.util.HashSet;
import java.util.Set;

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
        Set<String> result = new HashSet<>();
        result.add("open java in eng");
        result.add("open java in engines");
        when(dao.getImprovedCommandsForPattern("j-eng")).thenReturn(result);
        String command = cache.getPatternCommandForExecution("j-eng");
        verify(dao).getImprovedCommandsForPattern("j-eng");        
        assertEquals("open java in eng", command);
    }

}