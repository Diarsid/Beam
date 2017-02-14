/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import diarsid.beam.core.modules.data.daos.sql.H2DaoBatches;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testing.embedded.base.h2.H2TestDataBase;
import testing.embedded.base.h2.TestDataBase;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.SeePageCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchPauseCommand;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.stream.Collectors.toList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.SEE_WEBPAGE;
import static diarsid.beam.core.domain.entities.TimePeriod.SECONDS;
import static diarsid.beam.core.base.util.StringUtils.splitByWildcard;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoBatchesTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoBatchesTest.class);
    private static DaoBatches daoBatches;
    private static TestDataBase testDataBase;
    private static Initiator initiator;

    public H2DaoBatchesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        InnerIoEngine ioEngine = mock(InnerIoEngine.class);
        initiator = new Initiator();
        testDataBase = new H2TestDataBase("testBase");
        daoBatches = new H2DaoBatches(testDataBase, ioEngine);
        testDataBase.setupRequiredTable(
                "CREATE TABLE batches ( " +
                "bat_name   VARCHAR(300)    NOT NULL PRIMARY KEY )");
        testDataBase.setupRequiredTable(
                "CREATE TABLE batch_commands (" +
                "bat_name               VARCHAR(300)    NOT NULL," +
                "bat_command_order      INTEGER         NOT NULL, " +
                "bat_command_type       VARCHAR(50)     NOT NULL, " +
                "bat_command_original   VARCHAR(300)    NOT NULL, " +
                "bat_command_extended   VARCHAR(300)    NOT NULL,  " +
                "PRIMARY KEY (bat_name, bat_command_order) )");        
    }
    
    private static void setupTestData() {
        try (JdbcTransaction transact = testDataBase.transactionFactory().createTransaction()) {
            
            transact.doBatchUpdateVarargParams(
                    "INSERT INTO batches (bat_name) " +
                    "VALUES ( ? )", 
                    params("workspace"), 
                    params("tomcat"), 
                    params("open_space"));
            
            int[] modified = transact
                    .doBatchUpdateVarargParams(
                            "INSERT INTO batch_commands (" +
                            "       bat_name, " +
                            "       bat_command_type, " +
                            "       bat_command_order, " +
                            "       bat_command_original, " +
                            "       bat_command_extended ) " +
                            "VALUES ( ?, ?, ?, ?, ? ) ",
                            params("workspace", RUN_PROGRAM.name(),     0, "netbeans", "netbeans"),
                            params("workspace", OPEN_LOCATION.name(),   1, "projects", "projects"),
                            params("workspace", SEE_WEBPAGE.name(),     2, "google", "google"),
                            params("tomcat", RUN_PROGRAM.name(),    0, "mysql_server", "mysql_server"),
                            params("tomcat", RUN_PROGRAM.name(),    1, "tomcat", "tomcat"),
                            params("tomcat", BATCH_PAUSE.name(),    2, "3 SECONDS", "3 SECONDS"),
                            params("tomcat", SEE_WEBPAGE.name(),    3, "tomcat_root", "tomcat_root"),
                            params("open_space", OPEN_PATH.name(),        0, "books/common", "books/common"),
                            params("open_space", OPEN_LOCATION.name(),    1, "projects", "projects"),
                            params("open_space", OPEN_PATH.name(),        2, "content/tech", "content/tech"),
                            params("open_space", OPEN_LOCATION.name(),    3, "dev", "dev"));
            if ( modified.length != 11 ) {
                throw new IllegalArgumentException();
            }
        } catch (TransactionHandledSQLException ex) {
            
        }
    }
    
    private static void clearTestData() {
        try (JdbcTransaction transact = testDataBase.transactionFactory().createTransaction()) {
            transact.doUpdate("DELETE FROM batches");
            transact.doUpdate("DELETE FROM batch_commands");
        } catch (TransactionHandledSQLException ex) {
            
        }
    }
    
    @Before
    public void setUpCase() {
        setupTestData();
    }
    
    @After
    public void clearCase() {
        clearTestData();
    }
    
    @Test
    public void testIsNameFree() {
        boolean mustBeFree = daoBatches.isNameFree(initiator, "mysql");
        assertTrue(mustBeFree);
        
        boolean notFree = daoBatches.isNameFree(initiator, "WORKspace");
        assertFalse(notFree);
    }

    /**
     * Test of getBatchNamesByName method, of class H2DaoBatches.
     */
    @Test
    public void testGetBatchNamesByName() {
        List<String> names = daoBatches.getBatchNamesByNamePattern(initiator, "SPAce");
        assertTrue(names.size() == 2);
        assertTrue(names.contains("workspace"));
        assertTrue(names.contains("open_space"));
    }

    /**
     * Test of getBatchNamesByNameParts method, of class H2DaoBatches.
     */
    @Test
    public void testGetBatchNamesByNameParts() {
        List<String> names = daoBatches.getBatchNamesByNamePatternParts(initiator, splitByWildcard("w-spa"));
        assertTrue(names.size() == 1);
        assertTrue(names.contains("workspace"));
    }

    /**
     * Test of getBatchByName method, of class H2DaoBatches.
     */
    @Test
    public void testGetBatchByName() {
        List<ArgumentedCommand> commands = new ArrayList<>();
        commands.add(new RunProgramCommand("netbeans", "netbeans"));
        commands.add(new OpenLocationCommand("projects", "projects"));
        commands.add(new SeePageCommand("google", "google"));
        Batch batch = new Batch("workspace", commands);
        
        Batch restoredBatch = daoBatches.getBatchByName(initiator, "workspace").get();
        assertEquals(batch, restoredBatch);
    }
    
    @Test
    public void testGetBatchByName_precise() {
        
        Batch restoredBatch = daoBatches.getBatchByName(initiator, "tomcat").get();
        
        RunProgramCommand com0run = (RunProgramCommand) restoredBatch.getCommands().get(0).command();
        RunProgramCommand com1run = (RunProgramCommand) restoredBatch.getCommands().get(1).command();
        BatchPauseCommand com2pause = (BatchPauseCommand) restoredBatch.getCommands().get(2).command();
        SeePageCommand com3see = (SeePageCommand) restoredBatch.getCommands().get(3).command();
        
        assertEquals("mysql_server", com0run.argument().getOriginal());
        assertEquals("tomcat", com1run.argument().getOriginal());
        assertEquals("3 SECONDS", com2pause.stringifyOriginal());
        assertEquals(3, com2pause.getPauseDuration());
        assertEquals(SECONDS, com2pause.getTimePeriod());
        assertEquals("tomcat_root", com3see.page().getOriginal());
    }

    /**
     * Test of saveNewBatch method, of class H2DaoBatches.
     */
    @Test
    public void testSaveNewBatch() {
        List<ArgumentedCommand> commands = new ArrayList<>();
        commands.add(new RunProgramCommand("xxx", "xxx"));
        commands.add(new OpenLocationCommand("yyy", "yyy"));
        commands.add(new SeePageCommand("zzz", "zzz"));
        Batch batch = new Batch("aaa", commands);
        
        boolean saved = daoBatches.saveBatch(initiator, batch);
        assertTrue(saved);
        assertEquals(11 + 3, testDataBase.countRowsInTable("batch_commands"));
        assertEquals(3 + 1, testDataBase.countRowsInTable("batches"));
        
        List<ArgumentedCommand> oneMoreBatchCommands = new ArrayList<>();
        oneMoreBatchCommands.add(new OpenLocationCommand("QQQ", "QQQ"));
        oneMoreBatchCommands.add(new SeePageCommand("RRR", "RRR"));
        Batch sameNameBatch = new Batch("aaa", oneMoreBatchCommands);
        
        boolean notSaved = daoBatches.saveBatch(initiator, sameNameBatch);
        assertFalse(notSaved);
        assertEquals(11 + 3, testDataBase.countRowsInTable("batch_commands"));
        assertEquals(3 + 1, testDataBase.countRowsInTable("batches"));
    }

    /**
     * Test of removeBatch method, of class H2DaoBatches.
     */
    @Test
    public void testRemoveBatch() {
        boolean removed = daoBatches.removeBatch(initiator, "workspace");
        assertTrue(removed);
        assertEquals(11 - 3, testDataBase.countRowsInTable("batch_commands"));
        assertEquals(3 - 1, testDataBase.countRowsInTable("batches"));
        
        boolean notRemoved = daoBatches.removeBatch(initiator, "space");
        assertFalse(notRemoved);
        assertEquals(11 - 3, testDataBase.countRowsInTable("batch_commands"));
        assertEquals(3 - 1, testDataBase.countRowsInTable("batches"));
    }

    /**
     * Test of editBatchName method, of class H2DaoBatches.
     */
    @Test
    public void testEditBatchName() {
        Batch previous = daoBatches.getBatchByName(initiator, "workspace").get();
        
        boolean renamed = daoBatches.editBatchName(initiator, "workspace", "Environment");
        assertTrue(renamed);
        
        Batch after = daoBatches.getBatchByName(initiator, "Environment").get();
        
        assertEquals(previous.getCommandsQty(), after.getCommandsQty());
        for (int i = 0; i < previous.getCommandsQty(); i++) {
            assertEquals(previous.getCommands().get(i).command(), after.getCommands().get(i).command());
        }
    }

    /**
     * Test of editBatchCommands method, of class H2DaoBatches.
     */
    @Test
    public void testEditBatchCommands() {
        List<ArgumentedCommand> newCommands = new ArrayList<>();
        newCommands.add(new RunProgramCommand("xxx", "xxx"));
        newCommands.add(new OpenLocationCommand("yyy", "yyy"));
        
        boolean edited = daoBatches.editBatchCommands(initiator, "workspace", newCommands);
        assertTrue(edited);
        
        Batch after = daoBatches.getBatchByName(initiator, "workspace").get();
        assertEquals(newCommands.size(), after.getCommandsQty());
        for (int i = 0; i < after.getCommandsQty(); i++) {
            assertEquals(newCommands.get(i), after.getCommands().get(i).command());
        }
        
    }

    /**
     * Test of editBatchOneCommand method, of class H2DaoBatches.
     */
    @Test
    public void testEditBatchOneCommand() {
        Batch previous = daoBatches.getBatchByName(initiator, "workspace").get();
        
        ArgumentedCommand newCommand = new OpenLocationCommand("yyy", "yyy");
        int newCommandOrder = 1;
        boolean edited = daoBatches.editBatchOneCommand(initiator, "workspace", newCommandOrder, newCommand);
        assertTrue(edited);
        
        Batch after = daoBatches.getBatchByName(initiator, "workspace").get();
        assertEquals(previous.getCommandsQty(), after.getCommandsQty());
        for (int i = 0; i < previous.getCommandsQty(); i++) {
            if ( i == newCommandOrder ) {
                assertEquals(newCommand, after.getCommands().get(i).command());
            } else {
                assertEquals(previous.getCommands().get(i).command(), after.getCommands().get(i).command());
            }            
        }
    }

    /**
     * Test of getAllBatches method, of class H2DaoBatches.
     */
    @Test
    public void testGetAllBatches() {
        List<Batch> batchs = daoBatches.getAllBatches(initiator);
        assertTrue(batchs.size() == 3);
        List<String> names = batchs
                .stream()
                .map(batch -> batch.getName())
                .collect(toList());
        assertTrue(names.contains("workspace"));
        assertTrue(names.contains("tomcat"));
        assertTrue(names.contains("open_space"));
        
        List<ArgumentedCommand> allCommands = new ArrayList<>();
        batchs.stream()
                .map(batch -> batch.getCommands())
                .forEach(commands -> { 
                    allCommands.addAll(commands
                            .stream()
                            .map(batchedCommand -> batchedCommand.command())
                            .collect(toList())
                    );
                });
        assertTrue(allCommands.size() == 11);
    }

}