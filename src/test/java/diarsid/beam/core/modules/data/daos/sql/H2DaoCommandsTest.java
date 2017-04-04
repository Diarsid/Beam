/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

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
import diarsid.beam.core.base.control.io.commands.ExtendableCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseModel;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseVerifier;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseInitializer;
import diarsid.beam.core.modules.data.database.sql.SqlDataBaseModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.SEE_WEBPAGE;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoCommandsTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoCommandsTest.class);
    
    static DaoCommands dao;
    static Initiator initiator;
    static TestDataBase base;
    static InnerIoEngine ioEngine;
    

    public H2DaoCommandsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        initiator = new Initiator(41);
        base = new H2TestDataBase("commands-test");
        ioEngine = mock(InnerIoEngine.class);
        
        dao = new H2DaoCommands(base, ioEngine);
        SqlDataBaseModel model = new H2DataBaseModel();
        SqlDataBaseInitializer initializer = new H2DataBaseInitializer(ioEngine, base);
        DataBaseVerifier verifier = new H2DataBaseVerifier(initializer);
        List<String> reports = verifier.verify(base, model); 
        reports.stream().forEach(report -> logger.info(report));
    }
    
    @Before
    public void setupCase() throws Exception {
        base.transactionFactory()
                .createDisposableTransaction()
                .doBatchUpdateVarargParams(
                        "INSERT INTO commands ( com_type, com_original, com_extended ) " +
                        "VALUES ( ?, ?, ? ) ", 
                        params(RUN_PROGRAM.name(), "netb", "dev/NetBeans_8.2"),
                        params(OPEN_LOCATION.name(), "netb", "NetBeans_projects"),
                        params(OPEN_LOCATION.name(), "netbea-proj", "NetBeans_projects"),
                        params(OPEN_LOCATION.name(), "netb-proj", "NetBeans_projects"),
                        params(OPEN_PATH.name(), "proj/netb", "Projects/NetBeans"),
                        params(OPEN_PATH.name(), "proje/netbea", "Projects/NetBeans"),
                        params(OPEN_PATH.name(), "projects/netb", "Projects/NetBeans"),
                        params(OPEN_PATH.name(), "proj/beans", "Projects/NetBeans"),
                        params(OPEN_LOCATION.name(), "boo", "Books"),
                        params(OPEN_PATH.name(), "boo/tolk", "Books/Common/Tolkien"),
                        params(RUN_PROGRAM.name(), "tomc", "dev/Tomcat_8.5.5"),
                        params(CALL_BATCH.name(), "space", "Workspace"),
                        params(CALL_BATCH.name(), "sql", "mysql_server"),
                        params(RUN_PROGRAM.name(), "sql", "MySQL_5.7"),
                        params(SEE_WEBPAGE.name(), "fb", "Facebook"),
                        params(OPEN_LOCATION.name(), "java", "Java"),
                        params(OPEN_PATH.name(), "space", "Space"),
                        params(SEE_WEBPAGE.name(), "java", "Java SE 8 API"),
                        params(SEE_WEBPAGE.name(), "java-api", "Java SE 8 API"),
                        params(SEE_WEBPAGE.name(), "j-api", "Java SE 8 API"),
                        params(SEE_WEBPAGE.name(), "java-ee", "Java EE 7 API"));
    }
    
    @After
    public void clearCase() throws Exception {
        base.transactionFactory()
                .createDisposableTransaction()
                .doUpdate("DELETE FROM commands");
    }

    /**
     * Test of getByExactOriginalAndType method, of class H2DaoCommands.
     */
    @Test
    public void testGetByExactOriginalOfType() {
        Optional<ExtendableCommand> optCom = dao.getByExactOriginalAndType(initiator, "NEtb", RUN_PROGRAM);
        assertTrue(optCom.isPresent());
        assertTrue(optCom.get().type().equals(RUN_PROGRAM));
        assertEquals("dev/NetBeans_8.2", optCom.get().extendedArgument());
    }

    /**
     * Test of getByExactOriginalOfAnyType method, of class H2DaoCommands.
     */
    @Test
    public void testGetByExactOriginalOfAnyType() {
        List<ExtendableCommand> commands = dao.getByExactOriginalOfAnyType(initiator, "neTb");
        assertEquals(2, commands.size());
    }

    /**
     * Test of fullSearchByOriginalPattern method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPattern() {
        List<ExtendableCommand> commands = dao.fullSearchByOriginalPattern(initiator, "netb");
        assertEquals(7, commands.size());
    }

    /**
     * Test of fullSearchByOriginalPatternParts method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPatternParts() {
        List<ExtendableCommand> commands = dao.fullSearchByOriginalPattern(initiator, "net-pro");
        assertEquals(5, commands.size());
    }

    /**
     * Test of searchInOriginalByPatternAndType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPatternOfType() {
        List<ExtendableCommand> commands = dao
                .searchInOriginalByPatternAndType(initiator, "netb", OPEN_LOCATION);
        assertEquals(3, commands.size());
    }

    /**
     * Test of fullSearchByOriginalPatternPartsOfType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPatternPartsOfType() {
        List<ExtendableCommand> commands = 
                dao.searchInOriginalByPatternAndType(initiator, "net-pro", OPEN_LOCATION);
        assertEquals(2, commands.size());
    }

    /**
     * Test of fullSearchByExtendedPattern method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPattern() {
        List<ExtendableCommand> commands = dao.fullSearchByExtendedPattern(initiator, "tomca");
        assertEquals(1, commands.size());
        
        List<ExtendableCommand> commands1 = dao.fullSearchByExtendedPattern(initiator, "mysq");
        assertEquals(2, commands1.size());
    }

    /**
     * Test of fullSearchByExtendedPatternParts method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPatternParts() {
        List<ExtendableCommand> commands = dao.fullSearchByExtendedPattern(initiator, "se-api");
        assertEquals(3, commands.size());
    }

    /**
     * Test of searchInExtendedByPatternAndType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPatternOfType() {
        List<ExtendableCommand> commands = dao
                .searchInExtendedByPatternAndType(initiator, "mysq", RUN_PROGRAM);
        assertEquals(1, commands.size());
    }
    
    /**
     * Test of fullSearchByExtendedPatternPartsOfType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPatternPartsOfType() {
        List<ExtendableCommand> commands = dao
                .searchInExtendedByPatternAndType(initiator, "netbeans-proj", OPEN_LOCATION);
        assertEquals(3, commands.size());
    }

    /**
     * Test of save method, of class H2DaoCommands.
     */
    @Test
    public void testSave() {
        RunProgramCommand command = new RunProgramCommand("exc", "Util/Excel");
        
        int before = base.countRowsInTable("commands");
        boolean saved = dao.save(initiator, command);
        int after = base.countRowsInTable("commands");
        
        assertTrue(saved);
        assertEquals(before + 1, after);
    }

    /**
     * Test of delete method, of class H2DaoCommands.
     */
    @Test
    public void testDelete() {
        RunProgramCommand command = new RunProgramCommand("exc", "Util/Excel");
        
        int before = base.countRowsInTable("commands");
        boolean saved = dao.save(initiator, command);
        int after = base.countRowsInTable("commands");
        
        assertTrue(saved);
        assertEquals(before + 1, after);
        
        int beforeRemoving = base.countRowsInTable("commands");
        boolean removed = dao.delete(initiator, command);
        int afterRemoving = base.countRowsInTable("commands");
        
        assertTrue(removed);
        assertEquals(beforeRemoving - 1, afterRemoving);
    }

    /**
     * Test of deleteByExactOriginalOfAllTypes method, of class H2DaoCommands.
     */
    @Test
    public void testDeleteByExactOriginalOfAllTypes() {
        int before = base.countRowsInTable("commands");
        boolean removed = dao.deleteByExactOriginalOfAllTypes(initiator, "netb");
        int after = base.countRowsInTable("commands");
        
        assertTrue(removed);
        assertEquals(before - 2, after);
    }

    /**
     * Test of deleteByExactOriginalOfType method, of class H2DaoCommands.
     */
    @Test
    public void testDeleteByExactOriginalOfType() {
        int before = base.countRowsInTable("commands");
        boolean removed = dao.deleteByExactOriginalOfType(initiator, "netb", RUN_PROGRAM);
        int after = base.countRowsInTable("commands");
        
        assertTrue(removed);
        assertEquals(before - 1, after);
    }

}