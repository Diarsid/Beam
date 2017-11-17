/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import diarsid.beam.core.base.control.io.commands.executor.BrowsePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchPauseCommand;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BATCH_PAUSE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.domain.entities.TimePeriod.SECONDS;
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
        initiator = new Initiator(13, IN_MACHINE);
        testDataBase = new H2TestDataBase("testBase");
        daoBatches = new H2DaoBatches(testDataBase, ioEngine);
        SqlDataBaseModel dataBaseModel = new H2DataBaseModel();
        testDataBase.setupRequiredTable(dataBaseModel.table("batches").get().creationScript());
        testDataBase.setupRequiredTable(dataBaseModel.table("batch_commands").get().creationScript());        
    }
    
    private static void setupTestData() 
            throws TransactionHandledSQLException, TransactionHandledException {
        try (JdbcTransaction transact = testDataBase.transactionFactory().createTransaction()) {
            
            transact.doBatchUpdateVarargParams(
                    "INSERT INTO batches (bat_name) " +
                    "VALUES ( ? )", 
                    params("Workspace"), 
                    params("Tomcat"), 
                    params("open_space"));
            
            int[] modified = transact
                    .doBatchUpdateVarargParams("INSERT INTO batch_commands (" +
                            "   bat_name, " +
                            "   bat_command_type, " +
                            "   bat_command_order, " +
                            "   bat_command_original ) " +
                            "VALUES ( ?, ?, ?, ? ) ",
                            params("Workspace", RUN_PROGRAM,     0, "netbeans"),
                            params("Workspace", OPEN_LOCATION,   1, "projects"),
                            params("Workspace", BROWSE_WEBPAGE,     2, "google"),
                            params("Tomcat", RUN_PROGRAM,    0, "mysql_server"),
                            params("Tomcat", RUN_PROGRAM,    1, "tomcat"),
                            params("Tomcat", BATCH_PAUSE,    2, "3 SECONDS"),
                            params("Tomcat", BROWSE_WEBPAGE,    3, "tomcat_root"),
                            params("open_space", OPEN_LOCATION_TARGET,  0, "books/common"),
                            params("open_space", OPEN_LOCATION,         1, "projects"),
                            params("open_space", OPEN_LOCATION_TARGET,  2, "content/tech"),
                            params("open_space", OPEN_LOCATION,         3, "dev"));
            if ( modified.length != 11 ) {
                throw new IllegalArgumentException();
            }
        } 
    }
    
    private static void clearTestData() 
            throws TransactionHandledSQLException, TransactionHandledException {
        try (JdbcTransaction transact = testDataBase.transactionFactory().createTransaction()) {
            transact.doUpdate("DELETE FROM batches");
            transact.doUpdate("DELETE FROM batch_commands");
        } 
    }
    
    @Before
    public void setUpCase() throws Exception {
        setupTestData();
    }
    
    @After
    public void clearCase() throws Exception {
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
        assertEquals(2, names.size());
        assertTrue(names.contains("Workspace"));
        assertTrue(names.contains("open_space"));
    }

    /**
     * Test of getBatchNamesByNameParts method, of class H2DaoBatches.
     */
    @Test
    public void testGetBatchNamesByNamePattern_case1() {
        List<String> names = daoBatches.getBatchNamesByNamePattern(initiator, "wrkspa");
        assertEquals(1, names.size());
        assertTrue(names.contains("Workspace"));
    }
    
    @Test
    public void testGetBatchNamesByNamePattern_case2() {
        List<String> names = daoBatches.getBatchNamesByNamePattern(initiator, "spce");
        assertEquals(2, names.size());
        assertTrue(names.contains("Workspace"));
        assertTrue(names.contains("open_space"));
    }
    
    @Test
    public void testGetBatchNamesByNamePattern_case3_typo() {
        List<String> names = daoBatches.getBatchNamesByNamePattern(initiator, "spzce");
        assertEquals(2, names.size());
        assertTrue(names.contains("Workspace"));
        assertTrue(names.contains("open_space"));
    }

    /**
     * Test of getBatchByExactName method, of class H2DaoBatches.
     */
    @Test
    public void testGetBatchByName() {
        List<ExecutorCommand> commands = new ArrayList<>();
        commands.add(new RunProgramCommand("netbeans"));
        commands.add(new OpenLocationCommand("projects"));
        commands.add(new BrowsePageCommand("google"));
        Batch batch = new Batch("workspace", commands);
        
        Batch restoredBatch = daoBatches.getBatchByExactName(initiator, "workspace").get();
        assertEquals(batch, restoredBatch);
    }
    
    @Test
    public void testGetBatchByName_notFound() {
        
        Optional<Batch> restoredBatch = daoBatches.getBatchByExactName(initiator, "netb");
        assertFalse(restoredBatch.isPresent());
    }
    
    @Test
    public void testGetBatchByName_precise() {
        
        Batch restoredBatch = daoBatches.getBatchByExactName(initiator, "tomcat").get();
        
        RunProgramCommand com0run = (RunProgramCommand) restoredBatch.batchedCommands().get(0).unwrap();
        RunProgramCommand com1run = (RunProgramCommand) restoredBatch.batchedCommands().get(1).unwrap();
        BatchPauseCommand com2pause = (BatchPauseCommand) restoredBatch.batchedCommands().get(2).unwrap();
        BrowsePageCommand com3see = (BrowsePageCommand) restoredBatch.batchedCommands().get(3).unwrap();
        
        assertEquals("mysql_server", com0run.argument().original());
        assertEquals("tomcat", com1run.argument().original());
        assertEquals("3 seconds", com2pause.originalArgument());
        assertEquals(3, com2pause.duration());
        assertEquals(SECONDS, com2pause.timePeriod());
        assertEquals("tomcat_root", com3see.argument().original());
    }

    /**
     * Test of saveNewBatch method, of class H2DaoBatches.
     */
    @Test
    public void testSaveNewBatch() {
        List<ExecutorCommand> commands = new ArrayList<>();
        commands.add(new RunProgramCommand("xxx"));
        commands.add(new OpenLocationCommand("yyy"));
        commands.add(new BrowsePageCommand("zzz"));
        Batch batch = new Batch("aaa", commands);
        
        boolean saved = daoBatches.saveBatch(initiator, batch);
        assertTrue(saved);
        assertEquals(11 + 3, testDataBase.countRowsInTable("batch_commands"));
        assertEquals(3 + 1, testDataBase.countRowsInTable("batches"));
        
        List<ExecutorCommand> oneMoreBatchCommands = new ArrayList<>();
        oneMoreBatchCommands.add(new OpenLocationCommand("QQQ"));
        oneMoreBatchCommands.add(new BrowsePageCommand("RRR"));
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
        boolean removed = daoBatches.removeBatch(initiator, "Workspace");
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
        Batch previous = daoBatches.getBatchByExactName(initiator, "Workspace").get();
        
        boolean renamed = daoBatches.editBatchName(initiator, "Workspace", "Environment");
        assertTrue(renamed);
        
        Batch after = daoBatches.getBatchByExactName(initiator, "Environment").get();
        
        assertEquals(previous.getCommandsQty(), after.getCommandsQty());
        for (int i = 0; i < previous.getCommandsQty(); i++) {
            assertEquals(previous.batchedCommands().get(i).unwrap(), after.batchedCommands().get(i).unwrap());
        }
    }

    /**
     * Test of editBatchCommands method, of class H2DaoBatches.
     */
    @Test
    public void testEditBatchCommands() {
        List<ExecutorCommand> newCommands = new ArrayList<>();
        newCommands.add(new RunProgramCommand("xxx"));
        newCommands.add(new OpenLocationCommand("yyy"));
        
        boolean edited = daoBatches.editBatchCommands(initiator, "Workspace", newCommands);
        assertTrue(edited);
        
        Batch after = daoBatches.getBatchByExactName(initiator, "Workspace").get();
        assertEquals(newCommands.size(), after.getCommandsQty());
        for (int i = 0; i < after.getCommandsQty(); i++) {
            assertEquals(newCommands.get(i), after.batchedCommands().get(i).unwrap());
        }
        
    }

    /**
     * Test of editBatchOneCommand method, of class H2DaoBatches.
     */
    @Test
    public void testEditBatchOneCommand() {
        Batch previous = daoBatches.getBatchByExactName(initiator, "Workspace").get();
        
        ExecutorCommand newCommand = new OpenLocationCommand("yyy");
        int newCommandOrder = 1;
        boolean edited = daoBatches.editBatchOneCommand(initiator, "Workspace", newCommandOrder, newCommand);
        assertTrue(edited);
        
        Batch after = daoBatches.getBatchByExactName(initiator, "Workspace").get();
        assertEquals(previous.getCommandsQty(), after.getCommandsQty());
        for (int i = 0; i < previous.getCommandsQty(); i++) {
            if ( i == newCommandOrder ) {
                assertEquals(newCommand, after.batchedCommands().get(i).unwrap());
            } else {
                assertEquals(previous.batchedCommands().get(i).unwrap(), after.batchedCommands().get(i).unwrap());
            }            
        }
    }

    /**
     * Test of getAllBatches method, of class H2DaoBatches.
     */
    @Test
    public void testGetAllBatches() {
        List<Batch> batchs = daoBatches.getAllBatches(initiator);
        batchs.forEach(batch -> debug(batch.name() + " " + batch.stringifyCommands().stream().collect(joining(", "))));
        assertEquals(3, batchs.size());
        List<String> names = batchs
                .stream()
                .map(batch -> batch.name())
                .collect(toList());
        assertTrue(names.contains("Workspace"));
        assertTrue(names.contains("Tomcat"));
        assertTrue(names.contains("open_space"));
        
        List<ExecutorCommand> allCommands = new ArrayList<>();
        batchs.stream()
                .map(batch -> batch.batchedCommands())
                .forEach(commands -> { 
                    allCommands.addAll(commands
                            .stream()
                            .map(batchedCommand -> batchedCommand.unwrap())
                            .collect(toList())
                    );
                });
        assertTrue(allCommands.size() == 11);
    }

}