/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;
import java.util.stream.Stream;

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
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationTargetCommand;
import diarsid.beam.core.base.data.DataBaseActuator;
import diarsid.beam.core.base.data.DataBaseModel;
import diarsid.beam.core.base.data.SqlDataBaseModel;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.LocationSubPath;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.beam.core.modules.data.DaoLocationSubPaths;
import diarsid.beam.core.modules.data.DaoLocations;
import diarsid.beam.core.modules.data.sql.database.H2DataBaseModel;
import diarsid.jdbc.transactions.JdbcTransaction;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandLifePhase.NEW;
import static diarsid.beam.core.base.control.io.commands.executor.InvocationCommandTargetState.TARGET_NOT_FOUND;
import static diarsid.beam.core.base.data.DataBaseActuator.getActuatorFor;

/**
 *
 * @author Diarsid
 */
public class H2DaoLocationSubPathsV2Test {
    
    private static final Logger logger = LoggerFactory.getLogger(H2DaoLocationSubPathsV2Test.class);
    
    private static TestDataBase dataBase;
    private static InnerIoEngine ioEngine;
    private static DaoLocationSubPaths dao;
    private static DaoLocations daoLocations;
    private static DaoCommands daoCommands;
    private static Initiator initiator;
    
    public H2DaoLocationSubPathsV2Test() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        initiator = new Initiator(12, IN_MACHINE);
        dataBase = new H2TestDataBase();
        ioEngine = mock(InnerIoEngine.class);
        dao = new H2DaoLocationSubPathsV2(dataBase, ioEngine);
        daoCommands = new H2DaoCommandsV2(dataBase, ioEngine);
        daoLocations = new H2DaoLocationsV2(dataBase, ioEngine);
        
        DataBaseModel dataBaseModel = new H2DataBaseModel();
        
        DataBaseActuator actuator = getActuatorFor(dataBase, dataBaseModel);
        
        List<String> reports = actuator.actuateAndGetReport();
        reports.stream().forEach(report -> logger.info(report));
        assertEquals(reports.size(), ((SqlDataBaseModel) dataBaseModel).objects().size());
    }
    
    @Before
    public void setUp() {
        Stream.of(
                new Location("Books", "D:/Content/Books"), 
                new Location("Music", "D:/Content/Music"), 
                new Location("Games", "C:/Programs/Games"),
                new Location("Docs", "D:/Work/Docs"))
                .forEach(location -> daoLocations.saveNewLocation(initiator, location));
        List<OpenLocationTargetCommand> commands = asList(
                new OpenLocationTargetCommand("boks/jav", "Books/Java", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("bks/java", "Books/Java", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("boks/tolkn", "Books/Common/J.R.R_Tolkien", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("bok/tolkne", "Books/Common/J.R.R_Tolkien", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("boos/comm/tlkin", "Books/Common/J.R.R_Tolkien", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("gams/heros3", "Games/Heroes_3", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("game/her3", "Games/Heroes_3", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("gamse/wc2", "Games/WarCraft_2", NEW, TARGET_NOT_FOUND),
                new OpenLocationTargetCommand("music/shroe", "Music/Neoclassic/Howard_Shore", NEW, TARGET_NOT_FOUND));
        
        daoCommands.save(initiator, commands);
    }
    
    @After
    public void tearDown() throws Exception {
        JdbcTransaction transact = dataBase.transactionFactory().createTransaction();
        transact.doUpdate("DELETE FROM locations");
        transact.doUpdate("DELETE FROM commands");
        transact.commit();
    }

    @Test
    public void testGetSubPathesByPattern_token() {
        List<LocationSubPath> paths = dao.getSubPathesByPattern(initiator, "token");
        assertEquals(1, paths.size());
    }
    
    @Test
    public void testGetSubPathesByPattern_hers3() {
        List<LocationSubPath> paths = dao.getSubPathesByPattern(initiator, "hers3");
        assertEquals(2, paths.size());
    }
    
}
