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
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.database.sql.H2DataBaseModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_FOUND;
import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.jdbc.transactions.core.Params.params;

/**
 *
 * @author Diarsid
 */
public class H2DaoCommandsTest {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoCommandsTest.class);
    
    static DaoCommands dao;
    static Initiator initiator;
    static TestDataBase dataBase;
    static InnerIoEngine ioEngine;
    

    public H2DaoCommandsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        initiator = new Initiator(41);
        dataBase = new H2TestDataBase("commands-test");
        ioEngine = mock(InnerIoEngine.class);
        
        dao = new H2DaoCommands(dataBase, ioEngine);
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
        
        List<String> reports = actuator.actuateAndGetReport();
        reports.stream().forEach(report -> logger.info(report));
        assertEquals(reports.size(), ((SqlDataBaseModel) dataBaseModel).objects().size());
    }
    
    @Before
    public void setupCase() throws Exception {
        dataBase.transactionFactory()
                .createDisposableTransaction()
                .doBatchUpdateVarargParams(
                        "INSERT INTO commands ( com_type, com_original, com_extended ) " +
                        "VALUES ( ?, ?, ? ) ", 
                        params(RUN_PROGRAM, "netb", "dev/NetBeans_8.2"),
                        params(OPEN_LOCATION, "netpred", "Net_predictions"),
                        params(OPEN_LOCATION, "netb", "NetBeans_projects"),
                        params(OPEN_LOCATION, "netbeaproj", "NetBeans_projects"),
                        params(OPEN_LOCATION, "netbproj", "NetBeans_projects"),
                        params(OPEN_LOCATION_TARGET, "proj/netb", "Projects/NetBeans"),
                        params(OPEN_LOCATION_TARGET, "proje/netbea", "Projects/NetBeans"),
                        params(OPEN_LOCATION_TARGET, "projects/netb", "Projects/NetBeans"),
                        params(OPEN_LOCATION_TARGET, "proj/beans", "Projects/NetBeans"),
                        params(OPEN_LOCATION, "boo", "Books"),
                        params(OPEN_LOCATION_TARGET, "boo/tolk", "Books/Common/Tolkien"),
                        params(RUN_PROGRAM, "tomc", "dev/Tomcat_8.5.5"),
                        params(RUN_PROGRAM, "startomcat", "dev/Tomcat_8.5.5_start"),
                        params(RUN_PROGRAM, "stoptomct", "dev/Tomcat_8.5.5_stop"),
                        params(CALL_BATCH, "space", "Workspace"),
                        params(CALL_BATCH, "sql", "mysql_server"),
                        params(OPEN_LOCATION, "sqldepl", "Data_servers/developer/deploy"),
                        params(RUN_PROGRAM, "sqldev", "dev/Oracle_SQL_Developer"),
                        params(RUN_PROGRAM, "sqldv", "dev/Oracle_SQL_Developer"),
                        params(RUN_PROGRAM, "oradev", "dev/Oracle_SQL_Developer"),
                        params(RUN_PROGRAM, "sql", "MySQL_5.7"),
                        params(BROWSE_WEBPAGE, "fb", "Facebook"),
                        params(OPEN_LOCATION, "java", "Java"),
                        params(OPEN_LOCATION_TARGET, "space", "Space"),
                        params(BROWSE_WEBPAGE, "java", "Java SE 8 API"),
                        params(BROWSE_WEBPAGE, "javaapi", "Java SE 8 API"),
                        params(BROWSE_WEBPAGE, "japi", "Java SE 8 API"),
                        params(BROWSE_WEBPAGE, "javaee", "Java EE 7 API"));
    }
    
    @After
    public void clearCase() throws Exception {
        dataBase.transactionFactory()
                .createDisposableTransaction()
                .doUpdate("DELETE FROM commands");
    }

    /**
     * Test of getByExactOriginalAndType method, of class H2DaoCommands.
     */
    @Test
    public void testGetByExactOriginalOfType() {
        Optional<InvocationCommand> optCom = dao.getByExactOriginalAndType(initiator, "NEtb", RUN_PROGRAM);
        assertTrue(optCom.isPresent());
        assertTrue(optCom.get().type().equals(RUN_PROGRAM));
        assertEquals("dev/NetBeans_8.2", optCom.get().extendedArgument());
    }

    /**
     * Test of getByExactOriginalOfAnyType method, of class H2DaoCommands.
     */
    @Test
    public void testGetByExactOriginalOfAnyType() {
        List<InvocationCommand> commands = dao.getByExactOriginalOfAnyType(initiator, "neTb");
        assertEquals(2, commands.size());
    }

    /**
     * Test of searchInOriginalByPattern method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPattern() {
        List<InvocationCommand> commands = dao.searchInOriginalByPattern(initiator, "netb");
        assertEquals(7, commands.size());
    }

    /**
     * Test of fullSearchByOriginalPatternParts method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPatternParts() {
        List<InvocationCommand> commands = dao.searchInOriginalByPattern(initiator, "netpro");
        assertEquals(5, commands.size());
    }
    
    @Test
    public void testFullSearchByOriginalPatternParts_typo() {
        List<InvocationCommand> commands = dao.searchInOriginalByPattern(initiator, "netzpro");
        assertEquals(5, commands.size());
    }
    
    @Test
    public void testFullSearchByOriginalPatternParts_typo_1() {
        List<InvocationCommand> commands = dao.searchInOriginalByPattern(initiator, "netzpr"); 
        assertEquals(6, commands.size());
    }

    /**
     * Test of searchInOriginalByPatternAndType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPatternOfType() {
        List<InvocationCommand> commands = dao
                .searchInOriginalByPatternAndType(initiator, "netb", OPEN_LOCATION);
        assertEquals(3, commands.size());
    }

    /**
     * Test of fullSearchByOriginalPatternPartsOfType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByOriginalPatternPartsOfType() {
        List<InvocationCommand> commands = 
                dao.searchInOriginalByPatternAndType(initiator, "netpro", OPEN_LOCATION);
        assertEquals(2, commands.size());
    }

    /**
     * Test of searchInExtendedByPattern method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPattern_case1() {
        List<InvocationCommand> commands = dao.searchInExtendedByPattern(initiator, "tomca");
        assertEquals(3, commands.size());
    }
    
    @Test
    public void testFullSearchByExtendedPattern_case2() {        
        List<InvocationCommand> commands1 = dao.searchInExtendedByPattern(initiator, "mysq");
        assertEquals(2, commands1.size());
    }
    
    @Test
    public void testFullSearchByExtendedPattern_case3() {     
        List<InvocationCommand> commands3 = dao.searchInExtendedByPattern(initiator, "startmct");       
        commands3.forEach(command -> debug(command.stringify()));
        assertEquals(1, commands3.size());
    }
    
    @Test
    public void testFullSearchByExtendedPattern_case4() {     
        List<InvocationCommand> commands3 = dao.searchInExtendedByPattern(initiator, "stptmct");       
        commands3.forEach(command -> debug(command.stringify()));
        assertEquals(1, commands3.size());
    }

    /**
     * Test of fullSearchByExtendedPatternParts method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPatternParts() {
        List<InvocationCommand> commands = dao.searchInExtendedByPattern(initiator, "seapi");
        assertEquals(3, commands.size());
    }

    /**
     * Test of searchInExtendedByPatternAndType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPatternOfType_case1() {
        List<InvocationCommand> commands = dao
                .searchInExtendedByPatternAndType(initiator, "mysq", RUN_PROGRAM);
        assertEquals(1, commands.size());
    }
    
    @Test
    public void testFullSearchByExtendedPatternOfType_case2() {
        List<InvocationCommand> commands = dao
                .searchInExtendedByPatternAndType(initiator, "myq5", RUN_PROGRAM);
        assertEquals(1, commands.size());
    }
    
    @Test
    public void testFullSearchByExtendedPatternOfType_case3_typo() {
        List<InvocationCommand> commands = dao
                .searchInExtendedByPatternAndType(initiator, "mysaq5", RUN_PROGRAM);
        assertEquals(1, commands.size());
    }
    
    /**
     * Test of fullSearchByExtendedPatternPartsOfType method, of class H2DaoCommands.
     */
    @Test
    public void testFullSearchByExtendedPatternPartsOfType() {
        List<InvocationCommand> commands = dao
                .searchInExtendedByPatternAndType(initiator, "netbeansproj", OPEN_LOCATION);
        assertEquals(3, commands.size());
    }

    /**
     * Test of save method, of class H2DaoCommands.
     */
    @Test
    public void testSave() {
        RunProgramCommand command = new RunProgramCommand("exc", "Util/Excel", NEW, TARGET_FOUND);
        
        int before = dataBase.countRowsInTable("commands");
        boolean saved = dao.save(initiator, command);
        int after = dataBase.countRowsInTable("commands");
        
        assertTrue(saved);
        assertEquals(before + 2, after); // commands save its both versions original:extended and extended:extended
    }

    /**
     * Test of delete method, of class H2DaoCommands.
     */
    @Test
    public void testDelete() {
        RunProgramCommand command = new RunProgramCommand("exc", "Util/Excel", NEW, TARGET_FOUND);
        
        int before = dataBase.countRowsInTable("commands");
        boolean saved = dao.save(initiator, command);
        int after = dataBase.countRowsInTable("commands");
        
        assertTrue(saved);
        assertEquals(before + 2, after); // commands save its both versions original:extended and extended:extended
        
        int beforeRemoving = dataBase.countRowsInTable("commands");
        boolean removed = dao.delete(initiator, command);
        int afterRemoving = dataBase.countRowsInTable("commands");
        
        assertTrue(removed);
        assertEquals(beforeRemoving - 2, afterRemoving); // commands save its both versions original:extended and extended:extended
    }

    /**
     * Test of deleteByExactOriginalOfAllTypes method, of class H2DaoCommands.
     */
    @Test
    public void testDeleteByExactOriginalOfAllTypes() {
        int before = dataBase.countRowsInTable("commands");
        boolean removed = dao.deleteByExactOriginalOfAllTypes(initiator, "netb");
        int after = dataBase.countRowsInTable("commands");
        
        assertTrue(removed);
        assertEquals(before - 2, after);
    }

    /**
     * Test of deleteByExactOriginalOfType method, of class H2DaoCommands.
     */
    @Test
    public void testDeleteByExactOriginalOfType() {
        int before = dataBase.countRowsInTable("commands");
        boolean removed = dao.deleteByExactOriginalOfType(initiator, "netb", RUN_PROGRAM);
        int after = dataBase.countRowsInTable("commands");
        
        assertTrue(removed);
        assertEquals(before - 1, after);
    }

}